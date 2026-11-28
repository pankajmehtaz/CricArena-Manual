import { useEffect, useMemo, useState } from 'react';
import { AlertCircle, Building2, CheckCircle2, Loader2, Plus, X } from 'lucide-react';
import { collegeApi } from '../../services/apiClient';
import {
  fetchCitiesByState,
  fetchCollegesByState,
  fetchIndianStates,
  fetchMajorIndianUniversities,
  suggestCollegeCode,
} from '../../services/indiaGeoApi';
import SearchableDropdownInput from '../common/SearchableDropdownInput';

export default function CreateCollegeModal({ open, onClose, onCreated }) {
  const [name, setName] = useState('');
  const [code, setCode] = useState('');
  const [codeManuallyEdited, setCodeManuallyEdited] = useState(false);
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [address, setAddress] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [website, setWebsite] = useState('');

  // Directory API data & loading flags
  const [statesList, setStatesList] = useState([]);
  const [loadingStates, setLoadingStates] = useState(false);

  const [citiesList, setCitiesList] = useState([]);
  const [loadingCities, setLoadingCities] = useState(false);

  const [stateColleges, setStateColleges] = useState([]);
  const [majorUniversities, setMajorUniversities] = useState([]);
  const [loadingColleges, setLoadingColleges] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Fetch States & Major Indian Universities once when modal opens
  useEffect(() => {
    if (!open) return;
    let active = true;

    setLoadingStates(true);
    fetchIndianStates()
      .then((list) => {
        if (active) setStatesList(list);
      })
      .finally(() => {
        if (active) setLoadingStates(false);
      });

    fetchMajorIndianUniversities().then((list) => {
      if (active) setMajorUniversities(list);
    });

    return () => {
      active = false;
    };
  }, [open]);

  // Fetch Cities and State Colleges whenever State matches a known Indian State
  useEffect(() => {
    if (!open) return;
    const cleanState = state.trim();
    if (!cleanState) {
      setCitiesList([]);
      setStateColleges([]);
      return;
    }

    const matchedState = statesList.find(
      (s) => s.toLowerCase() === cleanState.toLowerCase()
    );
    if (!matchedState) return;

    let active = true;
    setLoadingCities(true);
    setLoadingColleges(true);

    Promise.all([
      fetchCitiesByState(matchedState),
      fetchCollegesByState(matchedState),
    ])
      .then(([cities, colleges]) => {
        if (!active) return;
        setCitiesList(cities);
        setStateColleges(colleges);
      })
      .finally(() => {
        if (active) {
          setLoadingCities(false);
          setLoadingColleges(false);
        }
      });

    return () => {
      active = false;
    };
  }, [open, state, statesList]);

  // Build college dropdown options:
  // If a state is loaded, show state colleges (prioritizing selected city at the top);
  // otherwise show major Indian universities.
  const collegeOptions = useMemo(() => {
    const source = stateColleges.length > 0 ? stateColleges : majorUniversities;
    const selectedCity = city.trim().toLowerCase();

    const mapped = source.map((item) => {
      const cityMatch =
        selectedCity && item.city && item.city.toLowerCase().includes(selectedCity);
      return {
        value: item.name,
        label: item.name,
        subtitle: item.subtitle || [item.city, item.state].filter(Boolean).join(', '),
        badge: cityMatch ? item.city : '',
        cityMatch,
        raw: item,
      };
    });

    if (selectedCity) {
      mapped.sort((a, b) => Number(b.cityMatch) - Number(a.cityMatch));
    }
    return mapped;
  }, [stateColleges, majorUniversities, city]);

  if (!open) return null;

  const resetForm = () => {
    setName('');
    setCode('');
    setCodeManuallyEdited(false);
    setCity('');
    setState('');
    setAddress('');
    setContactEmail('');
    setContactPhone('');
    setWebsite('');
    setError('');
    setSuccessMsg('');
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleStateChange = (newState) => {
    setState(newState);
    if (
      city &&
      citiesList.length > 0 &&
      !citiesList.some((c) => c.toLowerCase() === city.trim().toLowerCase())
    ) {
      setCity('');
    }
  };

  const handleSelectCollege = (option) => {
    const item = option?.raw || option;
    if (!item) return;

    setName(item.name);
    if (!codeManuallyEdited || !code.trim()) {
      setCode(suggestCollegeCode(item.name));
    }
    if (item.state && !state.trim()) {
      setState(item.state);
    }
    if (item.city && (!city.trim() || stateColleges.length > 0)) {
      setCity(item.city);
    }
    if (item.address && !address.trim()) {
      setAddress(item.address);
    }
    if (item.website && !website.trim()) {
      setWebsite(item.website);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (!name.trim()) {
      setError('College name is required.');
      return;
    }
    const cleanCode = code.trim().toUpperCase();
    if (!cleanCode || !/^[A-Z0-9-]+$/.test(cleanCode)) {
      setError('College code must be alphanumeric and may include hyphens (e.g. YMCA-FBD).');
      return;
    }
    if (!state.trim()) {
      setError('State is required.');
      return;
    }
    if (!city.trim()) {
      setError('City is required.');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        name: name.trim(),
        code: cleanCode,
        city: city.trim(),
        state: state.trim(),
        address: address.trim() || null,
        contactEmail: contactEmail.trim() || null,
        contactPhone: contactPhone.trim() || null,
        website: website.trim() || null,
      };

      const result = await collegeApi.create(payload);
      setSuccessMsg(`Institution "${result.name}" successfully enrolled!`);
      onCreated?.(result);
      setTimeout(() => {
        handleClose();
      }, 1000);
    } catch (err) {
      setError(err.message || 'Failed to enroll institution.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl border border-border bg-surface p-6 shadow-xl animate-in fade-in-50 zoom-in-95 duration-150 max-h-[92vh] overflow-y-auto">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Building2 className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Onboard Institution</h2>
              <p className="text-xs text-ink-secondary">
                Select from Indian college directory or enter details manually
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleClose}
            className="rounded-lg p-1.5 text-ink-muted hover:bg-surface-muted hover:text-ink cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {error && (
          <div className="mt-4 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/60 p-3 text-xs text-live">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {successMsg && (
          <div className="mt-4 flex items-start gap-2 rounded-xl border border-pitch-soft bg-pitch-soft/60 p-3 text-xs text-pitch">
            <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {/* State & City first so selecting a state immediately loads its cities and colleges */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                State / UT *
              </label>
              <SearchableDropdownInput
                required
                value={state}
                onChange={handleStateChange}
                options={statesList}
                loading={loadingStates}
                placeholder="Select state (e.g. Haryana)"
                emptyText="No matching state. Type to enter manually."
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                City *
              </label>
              <SearchableDropdownInput
                required
                value={city}
                onChange={setCity}
                options={citiesList}
                loading={loadingCities}
                placeholder={
                  state ? `Select city in ${state}` : 'Select state first or type city'
                }
                emptyText={
                  state
                    ? 'No matching city found. Keep typing to add manually.'
                    : 'Select a State first to load cities, or type manually.'
                }
              />
            </div>
          </div>

          {/* College / University Name (API directory) & Short Code */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="sm:col-span-2">
              <div className="flex items-center justify-between mb-1">
                <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary">
                  College / University Name *
                </label>
                {stateColleges.length > 0 && (
                  <span className="text-[10px] font-semibold text-brand-hover">
                    {stateColleges.length} in {state}
                  </span>
                )}
              </div>
              <SearchableDropdownInput
                required
                value={name}
                onChange={(val) => {
                  setName(val);
                  if (!codeManuallyEdited) {
                    setCode(suggestCollegeCode(val));
                  }
                }}
                onSelectOption={handleSelectCollege}
                options={collegeOptions}
                loading={loadingColleges}
                placeholder="Search or select college..."
                emptyText="College not in directory. Keep typing to enroll custom name."
                maxVisible={100}
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Short Code *
              </label>
              <input
                type="text"
                required
                value={code}
                onChange={(e) => {
                  setCodeManuallyEdited(true);
                  setCode(e.target.value.toUpperCase());
                }}
                placeholder="YMCA-FBD"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink uppercase placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Address / Campus Location (Optional)
            </label>
            <input
              type="text"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder="NH-2, Sector 6, Mathura Road"
              className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Contact Email (Optional)
              </label>
              <input
                type="email"
                value={contactEmail}
                onChange={(e) => setContactEmail(e.target.value)}
                placeholder="sports@jcboseust.ac.in"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Contact Phone (Optional)
              </label>
              <input
                type="text"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                placeholder="+91 129 231 0126"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-border">
            <button
              type="button"
              onClick={handleClose}
              className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-60 transition-colors cursor-pointer"
            >
              {submitting ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : (
                <Plus className="h-3.5 w-3.5" />
              )}
              {submitting ? 'Enrolling...' : 'Onboard Institution'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
