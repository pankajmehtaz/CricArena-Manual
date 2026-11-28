import { useEffect, useState } from 'react';
import { AlertCircle, X } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { matchApi, tournamentApi } from '../../services/apiClient';

const FORMAT_OPTIONS = [
  { value: 'T20', label: 'T20 (20 Overs)', defaultOvers: 20 },
  { value: 'ODI', label: 'ODI (50 Overs)', defaultOvers: 50 },
  { value: 'CUSTOM', label: 'Custom Overs', defaultOvers: 10 },
];

export default function CreateMatchModal({ open, onClose, onCreated }) {
  const { roleAssignment } = useAuth();
  const [tournaments, setTournaments] = useState([]);
  const [registeredTeams, setRegisteredTeams] = useState([]);
  const [loadingMeta, setLoadingMeta] = useState(false);
  const [loadingTeams, setLoadingTeams] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [tournamentId, setTournamentId] = useState('');
  const [teamAId, setTeamAId] = useState('');
  const [teamBId, setTeamBId] = useState('');
  const [matchDateTime, setMatchDateTime] = useState('');
  const [venue, setVenue] = useState('');
  const [format, setFormat] = useState('T20');
  const [maxOvers, setMaxOvers] = useState('20');

  useEffect(() => {
    if (!open) return;
    let active = true;
    setError('');
    setLoadingMeta(true);

    tournamentApi
      .getAll()
      .then((list) => {
        if (!active) return;
        const myCollegeId =
          roleAssignment?.role === 'COLLEGE_ADMIN' ? roleAssignment?.collegeId : null;
        const safe = Array.isArray(list)
          ? list.filter(
              (t) =>
                t.status !== 'COMPLETED' &&
                t.status !== 'CANCELLED' &&
                (!myCollegeId || String(t.collegeId) === String(myCollegeId))
            )
          : [];
        setTournaments(safe);
        if (safe.length > 0) {
          setTournamentId(String(safe[0].id));
        } else {
          setTournamentId('');
          setRegisteredTeams([]);
        }
      })
      .catch((err) => {
        if (active) setError(err.message || 'Unable to load tournaments.');
      })
      .finally(() => {
        if (active) setLoadingMeta(false);
      });

    return () => {
      active = false;
    };
  }, [open]);

  useEffect(() => {
    if (!open || !tournamentId) {
      setRegisteredTeams([]);
      return;
    }
    let active = true;
    setLoadingTeams(true);
    tournamentApi
      .getTeams(tournamentId)
      .then((regs) => {
        if (!active) return;
        const safeRegs = Array.isArray(regs) ? regs : [];
        setRegisteredTeams(safeRegs);
        setTeamAId(safeRegs[0] ? String(safeRegs[0].teamId) : '');
        setTeamBId(safeRegs[1] ? String(safeRegs[1].teamId) : '');
      })
      .catch((err) => {
        if (active) setError(err.message || 'Unable to load registered teams.');
      })
      .finally(() => {
        if (active) setLoadingTeams(false);
      });

    return () => {
      active = false;
    };
  }, [open, tournamentId]);

  if (!open) return null;

  const handleFormatChange = (nextFormat) => {
    setFormat(nextFormat);
    const matchOpt = FORMAT_OPTIONS.find((f) => f.value === nextFormat);
    if (matchOpt) {
      setMaxOvers(String(matchOpt.defaultOvers));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!tournamentId || !teamAId || !teamBId || !matchDateTime || !venue.trim()) {
      setError('Please complete all required match fields.');
      return;
    }
    if (teamAId === teamBId) {
      setError('Team A and Team B must be different teams.');
      return;
    }

    const oversNum = Number(maxOvers);
    if (!Number.isInteger(oversNum) || oversNum <= 0) {
      setError('Max overs must be a positive whole number.');
      return;
    }

    setSubmitting(true);
    try {
      const normalizedDate = matchDateTime.length === 16 ? `${matchDateTime}:00` : matchDateTime;
      const created = await matchApi.create({
        tournamentId: Number(tournamentId),
        teamAId: Number(teamAId),
        teamBId: Number(teamBId),
        matchDateTime: normalizedDate,
        venue: venue.trim(),
        format,
        maxOvers: oversNum,
      });
      onCreated?.(created);
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to schedule match.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/40 p-4 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="create-match-title"
    >
      <div className="w-full max-w-lg rounded-2xl border border-border bg-surface p-6 shadow-xl">
        <div className="flex items-center justify-between border-b border-border pb-3">
          <div>
            <h2 id="create-match-title" className="text-lg font-extrabold text-ink">
              Schedule New Match
            </h2>
            <p className="text-xs text-ink-secondary">
              Create a match between two teams registered in a tournament.
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

        {error && (
          <div className="mt-4 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/70 p-3 text-xs text-live">
            <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
              Tournament
            </label>
            <select
              value={tournamentId}
              onChange={(e) => setTournamentId(e.target.value)}
              disabled={loadingMeta}
              className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
            >
              {tournaments.length === 0 ? (
                <option value="">No active tournaments available</option>
              ) : (
                tournaments.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.name} ({t.startDate} to {t.endDate})
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Team A
              </label>
              <select
                value={teamAId}
                onChange={(e) => setTeamAId(e.target.value)}
                disabled={loadingTeams || registeredTeams.length === 0}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {registeredTeams.length === 0 ? (
                  <option value="">No registered teams</option>
                ) : (
                  registeredTeams.map((reg) => (
                    <option key={reg.teamId} value={reg.teamId}>
                      {reg.teamName}
                    </option>
                  ))
                )}
              </select>
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Team B
              </label>
              <select
                value={teamBId}
                onChange={(e) => setTeamBId(e.target.value)}
                disabled={loadingTeams || registeredTeams.length === 0}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {registeredTeams.length === 0 ? (
                  <option value="">No registered teams</option>
                ) : (
                  registeredTeams.map((reg) => (
                    <option key={reg.teamId} value={reg.teamId}>
                      {reg.teamName}
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          {!loadingTeams && tournamentId && registeredTeams.length < 2 && (
            <p className="rounded-xl bg-boundary-soft/60 px-3 py-2 text-xs text-boundary">
              At least two registered teams are required in the selected tournament to schedule a
              match.
            </p>
          )}

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Date &amp; Time
              </label>
              <input
                type="datetime-local"
                value={matchDateTime}
                onChange={(e) => setMatchDateTime(e.target.value)}
                required
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Venue
              </label>
              <input
                type="text"
                value={venue}
                onChange={(e) => setVenue(e.target.value)}
                placeholder="Central Cricket Ground"
                maxLength={150}
                required
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Format
              </label>
              <select
                value={format}
                onChange={(e) => handleFormatChange(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              >
                {FORMAT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-secondary">
                Overs
              </label>
              <input
                type="number"
                min={1}
                max={100}
                value={maxOvers}
                onChange={(e) => setMaxOvers(e.target.value)}
                required
                className="w-full rounded-xl border border-border bg-canvas px-3 py-2 text-sm text-ink focus:border-brand focus:bg-surface focus:outline-none"
              />
            </div>
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
              disabled={submitting || registeredTeams.length < 2}
              className="rounded-xl bg-brand px-4 py-2 text-sm font-bold text-white hover:bg-brand-hover disabled:opacity-50 cursor-pointer"
            >
              {submitting ? 'Scheduling...' : 'Create Match'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
