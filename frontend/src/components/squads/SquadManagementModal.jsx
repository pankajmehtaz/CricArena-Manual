import { useEffect, useState } from 'react';
import { AlertCircle, Check, Loader2, ShieldCheck, Users, X } from 'lucide-react';
import { playerApi, squadApi } from '../../services/apiClient';

export default function SquadManagementModal({
  open,
  onClose,
  tournamentId,
  teamId,
  tournamentName,
  teamName,
  onSquadSaved,
}) {
  const [availablePlayers, setAvailablePlayers] = useState([]);
  const [selectedPlayerIds, setSelectedPlayerIds] = useState(new Set());
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  useEffect(() => {
    if (!open || !tournamentId || !teamId) return;
    setError('');
    setFeedback('');
    setLoading(true);

    Promise.all([
      playerApi.getActiveByTeam(teamId).catch(() => playerApi.getByTeam(teamId)),
      squadApi.getSquad(tournamentId, teamId).catch(() => null),
    ])
      .then(([players, squadRes]) => {
        setAvailablePlayers(Array.isArray(players) ? players : []);
        if (squadRes && Array.isArray(squadRes.members)) {
          setSelectedPlayerIds(new Set(squadRes.members.map((m) => m.playerId)));
        } else {
          setSelectedPlayerIds(new Set());
        }
      })
      .catch((err) => {
        setError(err.message || 'Failed to load team players and tournament squad.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [open, tournamentId, teamId]);

  if (!open) return null;

  const togglePlayer = (playerId) => {
    setError('');
    setSelectedPlayerIds((prev) => {
      const next = new Set(prev);
      if (next.has(playerId)) {
        next.delete(playerId);
      } else {
        if (next.size >= 15) {
          setError('Squad cannot exceed the maximum limit of 15 players.');
          return prev;
        }
        next.add(playerId);
      }
      return next;
    });
  };

  const handleSave = async () => {
    if (selectedPlayerIds.size > 15) {
      setError('A tournament squad can have at most 15 players.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const result = await squadApi.setSquad(tournamentId, teamId, Array.from(selectedPlayerIds));
      setFeedback('Squad updated successfully.');
      onSquadSaved?.(result);
      setTimeout(() => {
        onClose();
      }, 800);
    } catch (err) {
      setError(err.message || 'Failed to save tournament squad.');
    } finally {
      setSubmitting(false);
    }
  };

  const count = selectedPlayerIds.size;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
      <div className="w-full max-w-2xl rounded-2xl border border-border bg-surface p-6 shadow-xl animate-in fade-in-50 zoom-in-95 duration-150">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-ink">Manage Tournament Squad</h2>
              <p className="text-xs text-ink-secondary">
                {teamName || 'Team'} • {tournamentName || 'Tournament'}
              </p>
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

        {/* Counter & Warning Bar */}
        <div className="mt-4 flex items-center justify-between rounded-xl border border-border bg-canvas p-3">
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-ink-secondary">Selected Squad Members:</span>
            <span
              className={`rounded-full px-2.5 py-0.5 text-xs font-bold tabular-nums ${
                count === 15
                  ? 'bg-pitch-soft text-pitch border border-pitch-soft'
                  : count > 15
                    ? 'bg-live-soft text-live border border-live-soft'
                    : 'bg-brand-soft text-brand-hover'
              }`}
            >
              {count} / 15 Max
            </span>
          </div>
          <span className="text-xs text-ink-muted">
            {count === 15 ? 'Full squad selected' : `${15 - count} spots remaining`}
          </span>
        </div>

        {error && (
          <div className="mt-3 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/60 p-3 text-xs text-live">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {feedback && (
          <div className="mt-3 flex items-start gap-2 rounded-xl border border-pitch-soft bg-pitch-soft/60 p-3 text-xs text-pitch font-semibold">
            <Check className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{feedback}</span>
          </div>
        )}

        {/* Players List */}
        <div className="mt-4 max-h-80 overflow-y-auto space-y-2 pr-1">
          {loading ? (
            <div className="py-12 text-center text-xs text-ink-muted">
              <Loader2 className="mx-auto h-6 w-6 animate-spin text-brand mb-2" />
              Loading team roster...
            </div>
          ) : availablePlayers.length === 0 ? (
            <div className="py-10 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No players found in this team's roster. Please add players first.
            </div>
          ) : (
            availablePlayers.map((player) => {
              const isSelected = selectedPlayerIds.has(player.id);
              return (
                <div
                  key={player.id}
                  onClick={() => togglePlayer(player.id)}
                  className={`flex items-center justify-between rounded-xl border p-3 text-xs transition-colors cursor-pointer select-none ${
                    isSelected
                      ? 'border-brand/40 bg-brand-soft/30'
                      : 'border-border bg-surface hover:bg-canvas'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className={`flex h-5 w-5 items-center justify-center rounded border transition-colors ${
                        isSelected
                          ? 'border-brand bg-brand text-white'
                          : 'border-border bg-canvas'
                      }`}
                    >
                      {isSelected && <Check className="h-3.5 w-3.5" />}
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-ink">{player.fullName}</span>
                        {player.jerseyNumber != null && (
                          <span className="rounded-full bg-surface-muted px-2 py-0.2 text-[10px] font-bold text-ink-secondary tabular-nums">
                            #{player.jerseyNumber}
                          </span>
                        )}
                      </div>
                      <p className="text-[11px] text-ink-secondary">
                        {player.playingRole?.replace('_', ' ') || 'Player'} • {player.battingStyle?.replace('_', ' ') || 'Right hand'}
                      </p>
                    </div>
                  </div>
                  <span
                    className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${
                      isSelected
                        ? 'bg-pitch-soft text-pitch'
                        : 'bg-surface-muted text-ink-muted'
                    }`}
                  >
                    {isSelected ? 'In Squad' : 'Available'}
                  </span>
                </div>
              );
            })
          )}
        </div>

        <div className="flex items-center justify-end gap-3 pt-4 mt-4 border-t border-border">
          <button
            type="button"
            onClick={onClose}
            className="rounded-xl border border-border bg-surface px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleSave}
            disabled={submitting || loading || count > 15}
            className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-5 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-50 transition-colors cursor-pointer"
          >
            {submitting && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
            {submitting ? 'Saving Squad...' : `Save Squad (${count}/15)`}
          </button>
        </div>
      </div>
    </div>
  );
}
