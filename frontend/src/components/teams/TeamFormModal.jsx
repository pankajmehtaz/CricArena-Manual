import { useEffect, useState } from 'react';
import { AlertCircle, Building2, Check, Loader2, X } from 'lucide-react';
import { collegeApi, teamApi } from '../../services/apiClient';

export default function TeamFormModal({
  open,
  onClose,
  onSaved,
  team = null,
  colleges = [],
  defaultCollegeId = null,
}) {
  const isEdit = Boolean(team);

  const [name, setName] = useState('');
  const [shortName, setShortName] = useState('');
  const [collegeId, setCollegeId] = useState('');
  const [homeGround, setHomeGround] = useState('');
  const [city, setCity] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [collegeList, setCollegeList] = useState(colleges);

  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFieldErrors({});
    setApiError('');

    if (colleges && colleges.length > 0) {
      setCollegeList(colleges);
    } else {
      collegeApi.getActive().then((list) => {
        setCollegeList(Array.isArray(list) ? list : []);
      }).catch(() => {});
    }

    if (team) {
      setName(team.name || '');
      setShortName(team.shortName || '');
      setCollegeId(team.collegeId ? String(team.collegeId) : '');
      setHomeGround(team.homeGround || '');
      setCity(team.city || '');
      setContactEmail(team.contactEmail || '');
    } else {
      setName('');
      setShortName('');
      setCollegeId(defaultCollegeId ? String(defaultCollegeId) : '');
      setHomeGround('');
      setCity('');
      setContactEmail('');
    }
  }, [open]);

  // When defaultCollegeId is provided or team has collegeId, lock college selection
  const lockedCollegeId = defaultCollegeId || team?.collegeId;
  const lockedCollege = lockedCollegeId
    ? collegeList.find((c) => String(c.id) === String(lockedCollegeId))
    : null;

  if (!open) return null;

  const validate = () => {
    const errs = {};
    const trimmedName = name.trim();
    if (!trimmedName) {
      errs.name = 'Team name is required.';
    } else if (trimmedName.length > 150) {
      errs.name = 'Team name must not exceed 150 characters.';
    }

    if (shortName.trim() && shortName.trim().length > 20) {
      errs.shortName = 'Short name must not exceed 20 characters.';
    }

    if (!collegeId) {
      errs.collegeId = 'College selection is required.';
    }

    if (homeGround.trim() && homeGround.trim().length > 150) {
      errs.homeGround = 'Home ground must not exceed 150 characters.';
    }

    if (city.trim() && city.trim().length > 100) {
      errs.city = 'City must not exceed 100 characters.';
    }

    if (contactEmail.trim()) {
      if (contactEmail.trim().length > 100) {
        errs.contactEmail = 'Contact email must not exceed 100 characters.';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contactEmail.trim())) {
        errs.contactEmail = 'Please provide a valid email address.';
      }
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    if (!validate()) return;

    const payload = {
      name: name.trim(),
      shortName: shortName.trim().toUpperCase() || null,
      collegeId: Number(collegeId),
      homeGround: homeGround.trim() || null,
      city: city.trim() || null,
      contactEmail: contactEmail.trim() || null,
    };

    setSubmitting(true);
    try {
      const saved = isEdit
        ? await teamApi.update(team.id, payload)
        : await teamApi.create(payload);
      onSaved?.(saved, isEdit);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors((prev) => ({ ...prev, ...err.validationErrors }));
      }
      setApiError(err.message || `Failed to ${isEdit ? 'update' : 'create'} team.`);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-ink/40 backdrop-blur-xs animate-in fade-in duration-150"
    >
      <div className="relative w-full max-w-lg rounded-3xl border border-border bg-surface p-6 sm:p-7 shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-border/60 pb-4">
          <div>
            <h2 className="text-xl font-extrabold text-ink">
              {isEdit ? 'Edit Team' : 'Create New Team'}
            </h2>
            <p className="mt-0.5 text-xs text-ink-secondary">
              {isEdit
                ? `Update profile information for ${team?.name}.`
                : 'Register a college cricket team in CricArena.'}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-full p-1.5 text-ink-secondary hover:bg-surface-muted hover:text-ink transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Global Error Banner */}
        {apiError && (
          <div className="mt-4 flex items-start gap-2.5 rounded-2xl border border-rose-200 bg-rose-50 p-3.5 text-xs text-rose-800">
            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600 mt-0.5" />
            <div className="flex-1 font-medium">{apiError}</div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          {/* Team Name */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
              Team Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              id="cricarena_team_name"
              name="cricarena_team_name"
              autoComplete="off"
              autoCorrect="off"
              autoCapitalize="words"
              spellCheck="false"
              autoFocus
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. BITS Warriors, Pilani Kings"
              maxLength={150}
              className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                fieldErrors.name ? 'border-rose-300 bg-rose-50/30' : 'border-border'
              }`}
            />
            {fieldErrors.name && (
              <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.name}</p>
            )}
          </div>

          {/* Short Name & College Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                Short Name / Code
              </label>
              <input
                type="text"
                autoComplete="off"
                autoCorrect="off"
                spellCheck="false"
                value={shortName}
                onChange={(e) => setShortName(e.target.value.toUpperCase())}
                placeholder="e.g. YT, RB"
                maxLength={20}
                className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-bold uppercase tracking-wider text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                  fieldErrors.shortName ? 'border-rose-300 bg-rose-50/30' : 'border-border'
                }`}
              />
              {fieldErrors.shortName && (
                <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.shortName}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                College Affiliation <span className="text-rose-500">*</span>
              </label>
              {lockedCollegeId ? (
                <div className="flex items-center justify-between rounded-xl border border-pitch-soft bg-pitch-soft/40 px-3.5 py-2.5 text-sm font-semibold text-pitch">
                  <div className="flex items-center gap-2 truncate">
                    <Building2 className="h-4 w-4 shrink-0 text-pitch" />
                    <span className="truncate">
                      {lockedCollege ? `${lockedCollege.name} (${lockedCollege.code})` : `College #${lockedCollegeId}`}
                    </span>
                  </div>
                  <span className="rounded-md bg-pitch/15 px-2 py-0.5 text-[10px] font-extrabold uppercase tracking-wider text-pitch">
                    Locked
                  </span>
                </div>
              ) : (
                <select
                  value={collegeId}
                  onChange={(e) => setCollegeId(e.target.value)}
                  className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                    fieldErrors.collegeId ? 'border-rose-300 bg-rose-50/30' : 'border-border'
                  }`}
                >
                  <option value="">Select College</option>
                  {collegeList.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} {c.code ? `(${c.code})` : ''}
                    </option>
                  ))}
                </select>
              )}
              {fieldErrors.collegeId && (
                <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.collegeId}</p>
              )}
            </div>
          </div>

          {/* City & Home Ground Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                City / Location
              </label>
              <input
                type="text"
                autoComplete="off"
                spellCheck="false"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                placeholder="e.g. Pilani, Rajasthan"
                maxLength={100}
                className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                  fieldErrors.city ? 'border-rose-300 bg-rose-50/30' : 'border-border'
                }`}
              />
              {fieldErrors.city && (
                <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.city}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                Home Ground
              </label>
              <input
                type="text"
                autoComplete="off"
                spellCheck="false"
                value={homeGround}
                onChange={(e) => setHomeGround(e.target.value)}
                placeholder="e.g. BITS Oval Ground"
                maxLength={150}
                className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                  fieldErrors.homeGround ? 'border-rose-300 bg-rose-50/30' : 'border-border'
                }`}
              />
              {fieldErrors.homeGround && (
                <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.homeGround}</p>
              )}
            </div>
          </div>

          {/* Contact Email */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
              Contact Email
            </label>
            <input
              type="email"
              autoComplete="off"
              spellCheck="false"
              value={contactEmail}
              onChange={(e) => setContactEmail(e.target.value)}
              placeholder="e.g. captain@bits-pilani.ac.in"
              maxLength={100}
              className={`w-full rounded-xl border bg-canvas px-3.5 py-2.5 text-sm font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface ${
                fieldErrors.contactEmail ? 'border-rose-300 bg-rose-50/30' : 'border-border'
              }`}
            />
            {fieldErrors.contactEmail && (
              <p className="mt-1 text-xs text-rose-600 font-medium">{fieldErrors.contactEmail}</p>
            )}
          </div>

          {/* Action Buttons */}
          <div className="mt-6 flex items-center justify-end gap-3 border-t border-border/60 pt-4">
            <button
              type="button"
              onClick={onClose}
              disabled={submitting}
              className="rounded-xl border border-border px-4 py-2.5 text-sm font-semibold text-ink-secondary hover:bg-surface-muted transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 rounded-xl bg-brand px-5 py-2.5 text-sm font-bold text-white shadow-xs hover:bg-brand-hover transition-colors disabled:opacity-50"
            >
              {submitting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Saving...</span>
                </>
              ) : (
                <>
                  <Check className="h-4 w-4" />
                  <span>{isEdit ? 'Save Changes' : 'Create Team'}</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
