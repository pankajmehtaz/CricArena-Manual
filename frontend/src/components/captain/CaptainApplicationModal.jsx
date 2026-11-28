import { useEffect, useState } from 'react';
import { AlertCircle, CheckCircle2, Loader2, Trophy, X } from 'lucide-react';
import { captainApplicationApi, tournamentApi } from '../../services/apiClient';

export default function CaptainApplicationModal({
  open,
  onClose,
  onSubmitted,
  defaultTournamentId = null,
  defaultCollegeId = null,
  tournaments = [],
}) {
  const [tournamentId, setTournamentId] = useState('');
  const [collegeId, setCollegeId] = useState('');
  const [contactNumber, setContactNumber] = useState('');
  const [reason, setReason] = useState('');
  const [experience, setExperience] = useState('');

  const [availableTournaments, setAvailableTournaments] = useState(tournaments);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    if (!open) return;
    setError('');
    setFieldErrors({});
    setContactNumber('');
    setReason('');
    setExperience('');

    if (defaultCollegeId) {
      setCollegeId(String(defaultCollegeId));
    }

    const syncSelection = (list) => {
      const published = list.filter(
        (t) => t.status === 'PUBLISHED' || t.status === 'SCHEDULED'
      );
      const valid = published.length > 0 ? published : list;
      setAvailableTournaments(valid);

      if (defaultTournamentId) {
        const strId = String(defaultTournamentId);
        setTournamentId(strId);
        const match = valid.find((t) => String(t.id) === strId);
        if (match?.collegeId) {
          setCollegeId(String(match.collegeId));
        }
      } else if (valid.length > 0) {
        setTournamentId(String(valid[0].id));
        if (valid[0].collegeId) {
          setCollegeId(String(valid[0].collegeId));
        }
      } else {
        setTournamentId('');
      }
    };

    if (tournaments && tournaments.length > 0) {
      syncSelection(tournaments);
    } else {
      tournamentApi
        .getAll()
        .then((list) => {
          const all = Array.isArray(list) ? list : [];
          const scoped = defaultCollegeId
            ? all.filter((t) => String(t.collegeId) === String(defaultCollegeId))
            : all;
          syncSelection(scoped);
        })
        .catch(() => {
          setAvailableTournaments([]);
        });
    }
  }, [open, defaultTournamentId, defaultCollegeId, tournaments]);

  // When tournament changes, auto-update collegeId if available
  useEffect(() => {
    if (tournamentId && availableTournaments.length > 0) {
      const match = availableTournaments.find((t) => String(t.id) === String(tournamentId));
      if (match && match.collegeId) {
        setCollegeId(String(match.collegeId));
      }
    }
  }, [tournamentId, availableTournaments]);

  if (!open) return null;

  const validate = () => {
    const errs = {};
    if (!tournamentId) errs.tournamentId = 'Tournament is required.';
    if (!collegeId) errs.collegeId = 'College is required.';
    if (!contactNumber.trim()) {
      errs.contactNumber = 'Contact number is required.';
    } else if (!/^[0-9+ -]{7,20}$/.test(contactNumber.trim())) {
      errs.contactNumber = 'Invalid phone format (7-20 digits, "+", spaces or hyphens).';
    }
    if (!reason.trim()) {
      errs.reason = 'Please explain why you want to be Captain.';
    } else if (reason.trim().length < 10) {
      errs.reason = 'Reason must be at least 10 characters long.';
    }
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      const response = await captainApplicationApi.submit({
        tournamentId: Number(tournamentId),
        collegeId: Number(collegeId),
        contactNumber: contactNumber.trim(),
        reason: reason.trim(),
        experience: experience.trim() || null,
      });
      onSubmitted?.(response);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors((prev) => ({ ...prev, ...err.validationErrors }));
      }
      setError(err.message || 'Failed to submit Captain application.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
      <div className="w-full max-w-lg rounded-2xl border border-border bg-surface p-6 shadow-xl animate-in fade-in-50 zoom-in-95 duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Apply for Team Captain</h2>
              <p className="text-xs text-ink-secondary">Lead a college cricket team in the tournament</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
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

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          {defaultTournamentId ? (
            <div className="rounded-xl border border-brand/25 bg-surface-mint p-3.5">
              <span className="block text-[10px] font-bold uppercase tracking-wider text-brand-hover">
                Applying For Tournament
              </span>
              <p className="mt-0.5 text-sm font-extrabold text-ink">
                {availableTournaments.find((t) => String(t.id) === String(tournamentId))?.name ||
                  'Selected Tournament'}
              </p>
              {availableTournaments.find((t) => String(t.id) === String(tournamentId))?.format && (
                <p className="text-[11px] text-ink-secondary mt-0.5">
                  Format:{' '}
                  {availableTournaments.find((t) => String(t.id) === String(tournamentId))?.format}
                </p>
              )}
            </div>
          ) : (
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Select Tournament
              </label>
              <select
                value={tournamentId}
                onChange={(e) => setTournamentId(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                <option value="">-- Choose a tournament --</option>
                {availableTournaments.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name} ({t.format})
                  </option>
                ))}
              </select>
              {fieldErrors.tournamentId && (
                <p className="mt-1 text-xs text-live">{fieldErrors.tournamentId}</p>
              )}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Contact Phone Number
            </label>
            <input
              type="text"
              required
              value={contactNumber}
              onChange={(e) => setContactNumber(e.target.value)}
              placeholder="+91 98765 43210"
              className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
            {fieldErrors.contactNumber && (
              <p className="mt-1 text-xs text-live">{fieldErrors.contactNumber}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Why should you be Captain? (Required)
            </label>
            <textarea
              required
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Describe your leadership experience, motivation, and captaincy vision..."
              className="w-full rounded-xl border border-border bg-canvas p-3 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
            {fieldErrors.reason && (
              <p className="mt-1 text-xs text-live">{fieldErrors.reason}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Cricket Experience & Achievements (Optional)
            </label>
            <textarea
              rows={2}
              value={experience}
              onChange={(e) => setExperience(e.target.value)}
              placeholder="e.g. Played district level, college team vice captain (2025)..."
              className="w-full rounded-xl border border-border bg-canvas p-3 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-border">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-60 transition-colors cursor-pointer"
            >
              {submitting && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
              {submitting ? 'Submitting...' : 'Submit Application'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
