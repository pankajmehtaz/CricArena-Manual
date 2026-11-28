import { useState } from 'react';
import { AlertCircle, Loader2, UserPlus, X } from 'lucide-react';
import { playerApi } from '../../services/apiClient';

const PLAYING_ROLES = [
  { value: 'BATSMAN', label: 'Batsman' },
  { value: 'BOWLER', label: 'Bowler' },
  { value: 'ALL_ROUNDER', label: 'All-Rounder' },
  { value: 'WICKET_KEEPER', label: 'Wicket-Keeper' },
];

const BATTING_STYLES = [
  { value: 'RIGHT_HAND', label: 'Right-Hand Bat' },
  { value: 'LEFT_HAND', label: 'Left-Hand Bat' },
];

const BOWLING_STYLES = [
  { value: 'RIGHT_ARM_FAST', label: 'Right-Arm Fast' },
  { value: 'RIGHT_ARM_MEDIUM', label: 'Right-Arm Medium' },
  { value: 'RIGHT_ARM_SPIN', label: 'Right-Arm Spin' },
  { value: 'LEFT_ARM_FAST', label: 'Left-Arm Fast' },
  { value: 'LEFT_ARM_SPIN', label: 'Left-Arm Spin' },
  { value: 'NONE', label: 'None' },
];

export default function AddPlayerModal({ open, onClose, teamId, onPlayerAdded }) {
  const [fullName, setFullName] = useState('');
  const [jerseyNumber, setJerseyNumber] = useState('');
  const [playingRole, setPlayingRole] = useState('BATSMAN');
  const [battingStyle, setBattingStyle] = useState('RIGHT_HAND');
  const [bowlingStyle, setBowlingStyle] = useState('NONE');
  const [contactEmail, setContactEmail] = useState('');
  const [contactPhone, setContactPhone] = useState('');

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  if (!open || !teamId) return null;

  const validate = () => {
    const errs = {};
    if (!fullName.trim()) {
      errs.fullName = 'Player full name is required.';
    } else if (fullName.trim().length > 100) {
      errs.fullName = 'Name cannot exceed 100 characters.';
    }

    if (jerseyNumber !== '') {
      const num = Number(jerseyNumber);
      if (Number.isNaN(num) || num < 0 || num > 999) {
        errs.jerseyNumber = 'Jersey number must be between 0 and 999.';
      }
    }

    if (contactEmail.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contactEmail.trim())) {
      errs.contactEmail = 'Invalid email address.';
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
      const payload = {
        teamId: Number(teamId),
        fullName: fullName.trim(),
        jerseyNumber: jerseyNumber !== '' ? Number(jerseyNumber) : null,
        playingRole,
        battingStyle,
        bowlingStyle: bowlingStyle === 'NONE' ? null : bowlingStyle,
        contactEmail: contactEmail.trim() || null,
        contactPhone: contactPhone.trim() || null,
      };

      const newPlayer = await playerApi.create(payload);
      onPlayerAdded?.(newPlayer);
      onClose();
    } catch (err) {
      if (err.validationErrors) {
        setFieldErrors((prev) => ({ ...prev, ...err.validationErrors }));
      }
      setError(err.message || 'Failed to add player to team.');
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
              <UserPlus className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Add Player to Roster</h2>
              <p className="text-xs text-ink-secondary">Enroll player into team permanent roster</p>
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
          <div className="grid grid-cols-3 gap-3">
            <div className="col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Full Name *
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="Virat Kohli"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
              {fieldErrors.fullName && (
                <p className="mt-1 text-xs text-live">{fieldErrors.fullName}</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Jersey #
              </label>
              <input
                type="number"
                min="0"
                max="999"
                value={jerseyNumber}
                onChange={(e) => setJerseyNumber(e.target.value)}
                placeholder="18"
                className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
              {fieldErrors.jerseyNumber && (
                <p className="mt-1 text-xs text-live">{fieldErrors.jerseyNumber}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Playing Role *
              </label>
              <select
                value={playingRole}
                onChange={(e) => setPlayingRole(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {PLAYING_ROLES.map((r) => (
                  <option key={r.value} value={r.value}>
                    {r.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Batting Style *
              </label>
              <select
                value={battingStyle}
                onChange={(e) => setBattingStyle(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {BATTING_STYLES.map((b) => (
                  <option key={b.value} value={b.value}>
                    {b.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
              Bowling Style
            </label>
            <select
              value={bowlingStyle}
              onChange={(e) => setBowlingStyle(e.target.value)}
              className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs text-ink focus:border-brand focus:bg-surface focus:outline-none"
            >
              {BOWLING_STYLES.map((b) => (
                <option key={b.value} value={b.value}>
                  {b.label}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Contact Email
              </label>
              <input
                type="email"
                value={contactEmail}
                onChange={(e) => setContactEmail(e.target.value)}
                placeholder="player@cricarena.com"
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-xs text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-ink-secondary mb-1">
                Contact Phone
              </label>
              <input
                type="text"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                placeholder="+91 98765 00000"
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-xs text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
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
              {submitting ? 'Adding...' : 'Add Player'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
