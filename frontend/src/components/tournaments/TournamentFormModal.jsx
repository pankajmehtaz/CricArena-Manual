import { useEffect, useState } from 'react';
import { AlertCircle, X } from 'lucide-react';
import { tournamentApi } from '../../services/apiClient';
import { useAuth } from '../../context/AuthContext';
import { useCollege } from '../../context/CollegeContext';

export const TOURNAMENT_FORMATS = [
  { value: 'LEAGUE', label: 'League' },
  { value: 'KNOCKOUT', label: 'Knockout' },
  { value: 'LEAGUE_KNOCKOUT', label: 'League + Knockout' },
];

export function formatTournamentType(format) {
  return TOURNAMENT_FORMATS.find((f) => f.value === format)?.label || format || '—';
}

export default function TournamentFormModal({ open, onClose, onSaved, tournament = null }) {
  const isEdit = Boolean(tournament);
  const { roleAssignment } = useAuth();
  const { colleges, selectedCollegeId } = useCollege();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [format, setFormat] = useState('LEAGUE');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [collegeId, setCollegeId] = useState('');
  const [registrationOpensAt, setRegistrationOpensAt] = useState('');
  const [registrationDeadline, setRegistrationDeadline] = useState('');

  const [fieldErrors, setFieldErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setFieldErrors({});
    setApiError('');

    if (tournament) {
      setName(tournament.name || '');
      setDescription(tournament.description || '');
      setFormat(tournament.format || 'LEAGUE');
      setStartDate(tournament.startDate || '');
      setEndDate(tournament.endDate || '');
      setCollegeId(tournament.collegeId ? String(tournament.collegeId) : '');
      setRegistrationOpensAt(tournament.registrationOpensAt ? tournament.registrationOpensAt.slice(0, 16) : '');
      setRegistrationDeadline(tournament.registrationDeadline ? tournament.registrationDeadline.slice(0, 16) : '');
    } else {
      setName('');
      setDescription('');
      setFormat('LEAGUE');
      setStartDate('');
      setEndDate('');
      // Default collegeId from user's role; Platform Admin defaults to Inter-College ('')
      const defaultCol = roleAssignment?.collegeId
        ? String(roleAssignment.collegeId)
        : '';
      setCollegeId(defaultCol);
      setRegistrationOpensAt('');
      setRegistrationDeadline('');
    }
  }, [open, tournament, roleAssignment, selectedCollegeId, colleges]);

  if (!open) return null;

  const validate = () => {
    const errs = {};
    const trimmedName = name.trim();
    if (!trimmedName) {
      errs.name = 'Tournament name is required.';
    } else if (trimmedName.length > 150) {
      errs.name = 'Tournament name must not exceed 150 characters.';
    }
    if (!format) {
      errs.format = 'Tournament format is required.';
    }
    if (!startDate) {
      errs.startDate = 'Start date is required.';
    }
    if (!endDate) {
      errs.endDate = 'End date is required.';
    }
    if (startDate && endDate && endDate < startDate) {
      errs.endDate = 'End date must not be before start date.';
    }
    if (registrationOpensAt && registrationDeadline && registrationDeadline < registrationOpensAt) {
      errs.registrationDeadline = 'Registration deadline cannot be before opening date.';
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
      description: description.trim() || null,
      format,
      startDate,
      endDate,
      collegeId: collegeId ? Number(collegeId) : null,
      registrationOpensAt: registrationOpensAt ? `${registrationOpensAt}:00` : null,
      registrationDeadline: registrationDeadline ? `${registrationDeadline}:00` : null,
    };

    setSubmitting(true);
    try {
      const saved = isEdit
        ? await tournamentApi.update(tournament.id, payload)
        : await tournamentApi.create(payload);
      onSaved?.(saved, isEdit);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors((prev) => ({ ...prev, ...err.validationErrors }));
      }
      setApiError(err.message || `Failed to ${isEdit ? 'update' : 'create'} tournament.`);
    } finally {
      setSubmitting(false);
    }
  };

  const isCollegeFixed = Boolean(roleAssignment?.collegeId);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="tournament-modal-title"
    >
      <div className="w-full max-w-lg rounded-2xl border border-border bg-surface p-6 shadow-xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between border-b border-border pb-3">
          <div>
            <h2 id="tournament-modal-title" className="text-lg font-extrabold text-ink">
              {isEdit ? 'Edit Tournament' : 'Create Tournament'}
            </h2>
            <p className="text-xs text-ink-secondary">
              {isEdit
                ? 'Update tournament schedule, format, or registration window.'
                : 'Set up a new cricket tournament and registration deadlines.'}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close modal"
            className="rounded-full p-1.5 text-ink-muted hover:bg-surface-muted hover:text-ink cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {apiError && (
          <div
            role="alert"
            className="mt-4 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/70 p-3 text-xs text-live"
          >
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{apiError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="mt-4 space-y-4">
          <div>
            <label
              htmlFor="tournament-name"
              className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
            >
              Tournament Name *
            </label>
            <input
              id="tournament-name"
              type="text"
              maxLength={150}
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="CricArena Premier Cup 2026"
              className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
            {fieldErrors.name && (
              <p className="mt-1 text-xs font-medium text-live">{fieldErrors.name}</p>
            )}
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label
                htmlFor="tournament-college"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Host College
              </label>
              <select
                id="tournament-college"
                value={collegeId}
                disabled={isCollegeFixed}
                onChange={(e) => setCollegeId(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none disabled:opacity-75"
              >
                <option value="">All Colleges (Inter-College)</option>
                {colleges.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.code})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label
                htmlFor="tournament-format"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Format *
              </label>
              <select
                id="tournament-format"
                value={format}
                onChange={(e) => setFormat(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {TOURNAMENT_FORMATS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label
                htmlFor="tournament-start"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Start Date *
              </label>
              <input
                id="tournament-start"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
              {fieldErrors.startDate && (
                <p className="mt-1 text-xs font-medium text-live">{fieldErrors.startDate}</p>
              )}
            </div>

            <div>
              <label
                htmlFor="tournament-end"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                End Date *
              </label>
              <input
                id="tournament-end"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
              {fieldErrors.endDate && (
                <p className="mt-1 text-xs font-medium text-live">{fieldErrors.endDate}</p>
              )}
            </div>
          </div>

          {/* Registration Window */}
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label
                htmlFor="reg-opens"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Registration Opens At
              </label>
              <input
                id="reg-opens"
                type="datetime-local"
                value={registrationOpensAt}
                onChange={(e) => setRegistrationOpensAt(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-xs text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="reg-deadline"
                className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
              >
                Registration Deadline
              </label>
              <input
                id="reg-deadline"
                type="datetime-local"
                value={registrationDeadline}
                onChange={(e) => setRegistrationDeadline(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-xs text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
              {fieldErrors.registrationDeadline && (
                <p className="mt-1 text-xs font-medium text-live">{fieldErrors.registrationDeadline}</p>
              )}
            </div>
          </div>

          <div>
            <label
              htmlFor="tournament-description"
              className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary"
            >
              Description
            </label>
            <textarea
              id="tournament-description"
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Inter-college limited-overs cricket tournament..."
              className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
            />
          </div>

          <div className="flex justify-end gap-2.5 border-t border-border pt-4">
            <button
              type="button"
              onClick={onClose}
              className="rounded-xl border border-border bg-surface px-4 py-2 text-sm font-semibold text-ink-secondary hover:bg-surface-muted cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="rounded-xl bg-brand px-4 py-2 text-sm font-bold text-white hover:bg-brand-hover disabled:opacity-50 cursor-pointer"
            >
              {submitting
                ? isEdit
                  ? 'Saving...'
                  : 'Creating...'
                : isEdit
                  ? 'Save Changes'
                  : 'Create Tournament'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
