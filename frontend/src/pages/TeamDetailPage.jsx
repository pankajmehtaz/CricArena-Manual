import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  AlertCircle,
  ArrowLeft,
  Building2,
  Calendar,
  CheckCircle2,
  Edit3,
  Mail,
  MapPin,
  Plus,
  PowerOff,
  RefreshCw,
  Shield,
  Trophy,
  UserCheck,
  UserPlus,
  Users,
  X,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import StatusBadge from '../components/common/StatusBadge';
import TeamFormModal from '../components/teams/TeamFormModal';
import AddPlayerModal from '../components/players/AddPlayerModal';
import { useAuth } from '../context/AuthContext';
import { collegeApi, playerApi, teamApi } from '../services/apiClient';
import {
  canManageTeams,
  formatTeamDate,
  getTeamInitials,
  getTeamMonogramClass,
} from '../utils/teamHelpers';

export default function TeamDetailPage() {
  const { teamId } = useParams();
  const { isAuthenticated, roleAssignment } = useAuth();

  const [team, setTeam] = useState(null);
  const [colleges, setColleges] = useState([]);
  const [players, setPlayers] = useState([]);
  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState(null);

  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deactivateModalOpen, setDeactivateModalOpen] = useState(false);
  const [addPlayerOpen, setAddPlayerOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const loadTeam = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const [teamData, collegeList, playerList] = await Promise.all([
        teamApi.getById(teamId),
        collegeApi.getActive().catch(() => []),
        playerApi.getByTeam(teamId).catch(() => []),
      ]);
      setTeam(teamData);
      setColleges(Array.isArray(collegeList) ? collegeList : []);
      setPlayers(Array.isArray(playerList) ? playerList : []);
    } catch (err) {
      setError(err.message || 'Failed to load team details.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, teamId]);

  useEffect(() => {
    loadTeam();
  }, [loadTeam]);

  const canManage = isAuthenticated && team && canManageTeams(roleAssignment, team);

  const handleSaved = (savedTeam) => {
    setTeam(savedTeam);
    setFeedback({
      type: 'success',
      text: `Team "${savedTeam.name}" updated successfully.`,
    });
    setTimeout(() => setFeedback(null), 4000);
  };

  const handleConfirmDeactivate = async () => {
    if (!team) return;
    setActionLoading(true);
    try {
      await teamApi.deactivate(team.id);
      setTeam((prev) => ({ ...prev, active: false }));
      setFeedback({
        type: 'success',
        text: `Team "${team.name}" has been deactivated.`,
      });
      setTimeout(() => setFeedback(null), 4000);
      setDeactivateModalOpen(false);
    } catch (err) {
      setFeedback({
        type: 'error',
        text: err.message || 'Failed to deactivate team.',
      });
    } finally {
      setActionLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="py-6">
        <AuthRequiredCard
          title="Sign in to view team details"
          description="Accessing team rosters, administration, and member registrations requires an active CricArena account."
        />
      </div>
    );
  }

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse py-6">
        <div className="h-6 w-32 rounded bg-surface-muted" />
        <div className="h-44 rounded-3xl border border-border bg-surface p-6" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="h-32 rounded-2xl border border-border bg-surface" />
          <div className="h-32 rounded-2xl border border-border bg-surface" />
          <div className="h-32 rounded-2xl border border-border bg-surface" />
        </div>
      </div>
    );
  }

  if (error || !team) {
    return (
      <div className="space-y-4 py-6">
        <Link
          to="/teams"
          className="inline-flex items-center gap-1.5 text-xs font-bold text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back to Teams</span>
        </Link>
        <div className="flex flex-col items-center justify-center rounded-3xl border border-rose-200 bg-rose-50 p-8 text-center text-rose-800">
          <AlertCircle className="h-10 w-10 text-rose-600 mb-2" />
          <p className="text-base font-extrabold">{error || 'Team not found'}</p>
          <p className="mt-1 text-xs text-rose-700">The requested team may have been removed or does not exist.</p>
          <button
            type="button"
            onClick={loadTeam}
            className="mt-4 rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white hover:bg-rose-700 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      </div>
    );
  }

  const initials = getTeamInitials(team.name, team.shortName);
  const monogramBg = getTeamMonogramClass(team.name);

  return (
    <div className="space-y-6">
      {/* Top Navigation */}
      <div className="flex items-center justify-between">
        <Link
          to="/teams"
          className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3 py-1.5 text-xs font-bold text-ink-secondary hover:bg-canvas hover:text-ink transition-colors shadow-xs"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back to Teams</span>
        </Link>

        <button
          type="button"
          onClick={loadTeam}
          className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3 py-1.5 text-xs font-bold text-ink-secondary hover:bg-canvas hover:text-ink transition-colors shadow-xs cursor-pointer"
          title="Refresh details"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          <span>Refresh</span>
        </button>
      </div>

      {/* Feedback Banner */}
      {feedback && (
        <div
          className={`flex items-start justify-between gap-3 rounded-2xl border p-4 text-xs font-medium animate-in fade-in duration-200 ${
            feedback.type === 'success'
              ? 'border-emerald-200 bg-emerald-50 text-emerald-900'
              : 'border-rose-200 bg-rose-50 text-rose-900'
          }`}
        >
          <div className="flex items-center gap-2">
            {feedback.type === 'success' ? (
              <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
            ) : (
              <AlertCircle className="h-4 w-4 text-rose-600 shrink-0" />
            )}
            <span>{feedback.text}</span>
          </div>
          <button
            type="button"
            onClick={() => setFeedback(null)}
            className="text-ink-muted hover:text-ink cursor-pointer"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Team Header Banner */}
      <div className="rounded-3xl border border-border bg-surface p-6 sm:p-8 shadow-[var(--shadow-card)]">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-5">
            <div
              className={`flex h-20 w-20 shrink-0 items-center justify-center rounded-3xl text-2xl font-black shadow-md ${monogramBg}`}
              aria-label={`${team.name} monogram`}
            >
              {initials}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2.5">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-ink tracking-tight">
                  {team.name}
                </h1>
                {team.shortName && (
                  <span className="rounded-lg bg-canvas px-2.5 py-1 text-xs font-black tracking-wider text-ink-secondary border border-border">
                    {team.shortName}
                  </span>
                )}
                <StatusBadge status={team.active ? 'ACTIVE' : 'INACTIVE'} />
              </div>

              <div className="mt-2 flex flex-wrap items-center gap-y-1 gap-x-4 text-xs text-ink-secondary">
                <span className="flex items-center gap-1.5 font-medium text-ink">
                  <Building2 className="h-3.5 w-3.5 text-brand" />
                  {team.collegeName || 'Unknown College'}
                  {team.collegeCode && (
                    <span className="rounded bg-brand-soft px-1.5 py-0.2 text-[10px] font-bold text-brand-hover">
                      {team.collegeCode}
                    </span>
                  )}
                </span>
                {team.city && (
                  <span className="flex items-center gap-1 text-ink-muted">
                    <MapPin className="h-3.5 w-3.5" />
                    {team.city}
                  </span>
                )}
                <span className="text-ink-muted font-mono">
                  ID: #{team.id}
                </span>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5 border-t border-border/60 pt-4 sm:border-t-0 sm:pt-0">
            {canManage && (
              <>
                <button
                  type="button"
                  onClick={() => setAddPlayerOpen(true)}
                  className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2.5 text-xs font-bold text-white hover:bg-brand-hover transition-colors shadow-xs cursor-pointer"
                >
                  <UserPlus className="h-3.5 w-3.5" />
                  <span>Add Player</span>
                </button>

                <button
                  type="button"
                  onClick={() => setEditModalOpen(true)}
                  className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-4 py-2.5 text-xs font-bold text-ink hover:bg-surface-muted transition-colors shadow-xs cursor-pointer"
                >
                  <Edit3 className="h-3.5 w-3.5 text-brand" />
                  <span>Edit Team</span>
                </button>

                {team.active ? (
                  <button
                    type="button"
                    onClick={() => setDeactivateModalOpen(true)}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-4 py-2.5 text-xs font-bold text-rose-700 hover:bg-rose-100 transition-colors cursor-pointer"
                  >
                    <PowerOff className="h-3.5 w-3.5 text-rose-600" />
                    <span>Deactivate</span>
                  </button>
                ) : (
                  <span className="rounded-xl bg-canvas px-3 py-2 text-xs font-medium text-ink-muted border border-border">
                    Team is Inactive
                  </span>
                )}
              </>
            )}
          </div>
        </div>
      </div>

      {/* Team Details & Metadata Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* College Affiliation */}
        <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-ink-secondary border-b border-border/60 pb-2.5">
            <Building2 className="h-4 w-4 text-brand" />
            <span>College Affiliation</span>
          </div>
          <div className="space-y-1.5 text-xs">
            <p className="font-extrabold text-sm text-ink">{team.collegeName || '—'}</p>
            <div className="flex items-center justify-between text-ink-secondary">
              <span>College Code:</span>
              <span className="font-mono font-bold text-ink">{team.collegeCode || '—'}</span>
            </div>
            <div className="flex items-center justify-between text-ink-secondary">
              <span>College ID:</span>
              <span className="font-mono text-ink">#{team.collegeId}</span>
            </div>
          </div>
        </div>

        {/* Location & Home Ground */}
        <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-ink-secondary border-b border-border/60 pb-2.5">
            <MapPin className="h-4 w-4 text-brand" />
            <span>Ground & Location</span>
          </div>
          <div className="space-y-1.5 text-xs">
            <div className="flex items-center justify-between text-ink-secondary">
              <span>Home Ground:</span>
              <span className="font-bold text-ink text-right">{team.homeGround || 'Not specified'}</span>
            </div>
            <div className="flex items-center justify-between text-ink-secondary">
              <span>City / Region:</span>
              <span className="font-medium text-ink">{team.city || 'Not specified'}</span>
            </div>
          </div>
        </div>

        {/* Contact & Administration */}
        <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-ink-secondary border-b border-border/60 pb-2.5">
            <Shield className="h-4 w-4 text-brand" />
            <span>Captain & Administration</span>
          </div>
          <div className="space-y-1.5 text-xs">
            <div className="flex items-center justify-between text-ink-secondary">
              <span>Team Captain:</span>
              <span className="font-bold text-brand-hover">
                {team.captainName || (roleAssignment?.teamId === team.id ? 'You' : 'Not Assigned')}
              </span>
            </div>
            <div className="flex items-center justify-between text-ink-secondary">
              <span>Contact Email:</span>
              <span className="font-mono text-ink text-[11px] truncate max-w-[160px]" title={team.contactEmail}>
                {team.contactEmail || '—'}
              </span>
            </div>
            <div className="flex items-center justify-between text-ink-secondary">
              <span>Registered On:</span>
              <span className="text-ink">{formatTeamDate(team.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Squad & Player Roster Section */}
      <div className="rounded-3xl border border-border bg-surface p-6 sm:p-7 shadow-[var(--shadow-card)] space-y-5">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 border-b border-border/60 pb-4">
          <div className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-xl bg-pitch-soft text-pitch">
              <Users className="h-4 w-4" />
            </span>
            <div>
              <h2 className="text-lg font-extrabold text-ink">Permanent Player Roster</h2>
              <p className="text-xs text-ink-secondary">
                {players.length} registered player(s) enrolled in {team.name}.
              </p>
            </div>
          </div>

          {canManage && (
            <button
              type="button"
              onClick={() => setAddPlayerOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-full bg-brand px-3.5 py-1.5 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
            >
              <Plus className="h-3.5 w-3.5" />
              Add Player to Roster
            </button>
          )}
        </div>

        {/* Real Players List */}
        {players.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-border bg-canvas/60 p-8 text-center text-xs text-ink-muted">
            No players currently enrolled in this team roster. Click "Add Player" to enroll.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {players.map((p) => (
              <div
                key={p.id}
                className="rounded-2xl border border-border bg-canvas p-4 text-xs space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-ink text-sm">{p.fullName}</span>
                    {p.jerseyNumber != null && (
                      <span className="rounded-full bg-surface-muted px-2 py-0.2 text-[10px] font-bold text-ink-secondary tabular-nums">
                        #{p.jerseyNumber}
                      </span>
                    )}
                  </div>
                  <span
                    className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${
                      p.active ? 'bg-pitch-soft text-pitch' : 'bg-surface-muted text-ink-muted'
                    }`}
                  >
                    {p.active ? 'Active' : 'Inactive'}
                  </span>
                </div>

                <div className="space-y-0.5 text-ink-secondary text-[11px]">
                  <div>Role: <span className="font-semibold text-ink">{p.playingRole?.replace('_', ' ') || 'Player'}</span></div>
                  <div>Batting: <span className="text-ink">{p.battingStyle?.replace('_', ' ') || 'Right hand'}</span></div>
                  {p.bowlingStyle && (
                    <div>Bowling: <span className="text-ink">{p.bowlingStyle.replace('_', ' ')}</span></div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Edit Modal */}
      <TeamFormModal
        open={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        onSaved={handleSaved}
        team={team}
        colleges={colleges}
        defaultCollegeId={team.collegeId}
      />

      {/* Add Player Modal */}
      <AddPlayerModal
        open={addPlayerOpen}
        onClose={() => setAddPlayerOpen(false)}
        teamId={team.id}
        onPlayerAdded={(newPlayer) => {
          setFeedback({
            type: 'success',
            text: `Player ${newPlayer.fullName} enrolled into roster.`,
          });
          loadTeam();
        }}
      />

      {/* Deactivate Confirmation Modal */}
      {deactivateModalOpen && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in duration-150"
        >
          <div className="relative w-full max-w-md rounded-3xl border border-border bg-surface p-6 shadow-2xl">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-rose-100 text-rose-600">
                <PowerOff className="h-5 w-5" />
              </div>
              <div>
                <h3 className="text-base font-extrabold text-ink">Deactivate Team?</h3>
                <p className="text-xs text-ink-secondary mt-0.5">
                  Are you sure you want to deactivate{' '}
                  <span className="font-bold text-ink">{team.name}</span>?
                </p>
              </div>
            </div>

            <p className="mt-4 rounded-xl bg-canvas p-3 text-xs text-ink-secondary leading-relaxed border border-border">
              Deactivating will mark this team as inactive. Inactive teams cannot be registered
              for new tournaments or scheduled for upcoming matches.
            </p>

            <div className="mt-6 flex items-center justify-end gap-3 border-t border-border/60 pt-4">
              <button
                type="button"
                onClick={() => setDeactivateModalOpen(false)}
                disabled={actionLoading}
                className="rounded-xl border border-border px-4 py-2 text-xs font-semibold text-ink-secondary hover:bg-surface-muted transition-colors disabled:opacity-50 cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmDeactivate}
                disabled={actionLoading}
                className="inline-flex items-center gap-1.5 rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-rose-700 transition-colors disabled:opacity-50 cursor-pointer"
              >
                {actionLoading ? (
                  <>
                    <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                    <span>Deactivating...</span>
                  </>
                ) : (
                  <>
                    <PowerOff className="h-3.5 w-3.5" />
                    <span>Deactivate</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
