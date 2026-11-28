import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  AlertCircle,
  ArrowLeft,
  Calendar,
  CheckCircle2,
  Clock,
  Edit3,
  MapPin,
  Plus,
  RefreshCw,
  Search,
  Shield,
  Trash2,
  Trophy,
  Users,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import StatusBadge from '../components/common/StatusBadge';
import MatchCard from '../components/matches/MatchCard';
import TournamentFormModal, {
  formatTournamentType,
} from '../components/tournaments/TournamentFormModal';
import CaptainApplicationModal from '../components/captain/CaptainApplicationModal';
import SquadManagementModal from '../components/squads/SquadManagementModal';
import { useAuth } from '../context/AuthContext';
import { matchApi, teamApi, tournamentApi } from '../services/apiClient';
import {
  canManageMatches,
  getTeamBadgeClass,
  getTeamInitials,
} from '../utils/matchHelpers';

export default function TournamentDetailPage() {
  const { tournamentId } = useParams();
  const { isAuthenticated, roleAssignment } = useAuth();
  const canManage = isAuthenticated && canManageMatches(roleAssignment);
  const isCaptain = roleAssignment?.role === 'TEAM_CAPTAIN';
  const myTeamId = roleAssignment?.teamId;

  const [tournament, setTournament] = useState(null);
  const [registeredTeams, setRegisteredTeams] = useState([]);
  const [activeTeams, setActiveTeams] = useState([]);
  const [matches, setMatches] = useState([]);
  const [scorecardsByMatchId, setScorecardsByMatchId] = useState({});

  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState(null); // { type: 'success' | 'error', text }
  const [teamSearch, setTeamSearch] = useState('');
  const [busyTeamId, setBusyTeamId] = useState(null);
  const [editOpen, setEditOpen] = useState(false);

  // Modals for Phase 7
  const [applyModalOpen, setApplyModalOpen] = useState(false);
  const [squadModalOpen, setSquadModalOpen] = useState(false);
  const [squadTeam, setSquadTeam] = useState(null);

  const loadDetails = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const [tData, regList, activeList, matchList] = await Promise.all([
        tournamentApi.getById(tournamentId),
        tournamentApi.getTeams(tournamentId),
        teamApi.getActive(),
        tournamentApi.getMatches(tournamentId),
      ]);

      setTournament(tData);
      setRegisteredTeams(Array.isArray(regList) ? regList : []);
      setActiveTeams(Array.isArray(activeList) ? activeList : []);

      const safeMatches = Array.isArray(matchList) ? matchList : [];
      setMatches(safeMatches);

      const scorecardTargets = safeMatches.filter(
        (m) =>
          m.status === 'IN_PROGRESS' ||
          m.status === 'COMPLETED' ||
          m.status === 'ABANDONED'
      );
      if (scorecardTargets.length > 0) {
        const settled = await Promise.allSettled(
          scorecardTargets.map((m) => matchApi.getScorecard(m.id))
        );
        const map = {};
        settled.forEach((res, idx) => {
          if (res.status === 'fulfilled' && res.value) {
            map[scorecardTargets[idx].id] = res.value;
          }
        });
        setScorecardsByMatchId(map);
      } else {
        setScorecardsByMatchId({});
      }
    } catch (err) {
      setError(err.message || 'Failed to load tournament details.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, tournamentId]);

  useEffect(() => {
    loadDetails();
  }, [loadDetails]);

  const registeredTeamIds = useMemo(
    () => new Set(registeredTeams.map((reg) => reg.teamId)),
    [registeredTeams]
  );

  const isMyTeamRegistered = Boolean(myTeamId && registeredTeamIds.has(Number(myTeamId)));

  const myActiveTeam = useMemo(
    () => activeTeams.find((team) => String(team.id) === String(myTeamId)) || null,
    [activeTeams, myTeamId]
  );

  const availableTeams = useMemo(() => {
    const q = teamSearch.trim().toLowerCase();
    return activeTeams
      .filter((team) => !registeredTeamIds.has(team.id))
      .filter((team) => {
        if (!q) return true;
        return (
          team.name?.toLowerCase().includes(q) ||
          team.shortName?.toLowerCase().includes(q) ||
          team.city?.toLowerCase().includes(q) ||
          team.collegeName?.toLowerCase().includes(q)
        );
      })
      .sort((a, b) => {
        const aIsMine = String(a.id) === String(myTeamId);
        const bIsMine = String(b.id) === String(myTeamId);
        if (aIsMine !== bIsMine) return aIsMine ? -1 : 1;
        return (a.name || '').localeCompare(b.name || '');
      });
  }, [activeTeams, registeredTeamIds, teamSearch, myTeamId]);

  // Check Registration Window
  const { registrationStatus, registrationClosed } = useMemo(() => {
    if (!tournament) return { registrationStatus: 'UNKNOWN', registrationClosed: false };
    if (tournament.status === 'COMPLETED' || tournament.status === 'CANCELLED') {
      return { registrationStatus: 'CLOSED', registrationClosed: true };
    }
    const now = new Date();
    if (tournament.registrationOpensAt && new Date(tournament.registrationOpensAt) > now) {
      return { registrationStatus: 'UPCOMING', registrationClosed: true };
    }
    if (tournament.registrationDeadline && new Date(tournament.registrationDeadline) < now) {
      return { registrationStatus: 'DEADLINE_PASSED', registrationClosed: true };
    }
    return { registrationStatus: 'OPEN', registrationClosed: false };
  }, [tournament]);

  const handleRegisterTeam = async (team) => {
    setFeedback(null);
    setBusyTeamId(team.id);
    try {
      const createdReg = await tournamentApi.registerTeam(tournamentId, team.id);
      setRegisteredTeams((prev) => [...prev, createdReg]);
      setFeedback({
        type: 'success',
        text: `${team.name} registered in ${tournament?.name || 'tournament'}.`,
      });
    } catch (err) {
      setFeedback({
        type: 'error',
        text: err.message || `Failed to register ${team.name}.`,
      });
    } finally {
      setBusyTeamId(null);
    }
  };

  const handleRemoveTeam = async (reg) => {
    setFeedback(null);
    setBusyTeamId(reg.teamId);
    try {
      await tournamentApi.removeTeam(tournamentId, reg.teamId);
      setRegisteredTeams((prev) => prev.filter((item) => item.teamId !== reg.teamId));
      setFeedback({
        type: 'success',
        text: `${reg.teamName} removed from ${tournament?.name || 'tournament'}.`,
      });
    } catch (err) {
      setFeedback({
        type: 'error',
        text: err.message || `Failed to remove ${reg.teamName}.`,
      });
    } finally {
      setBusyTeamId(null);
    }
  };

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-3">
        <Link
          to="/tournaments"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-ink-secondary hover:text-ink transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Tournaments
        </Link>

        {isAuthenticated && (
          <button
            type="button"
            onClick={loadDetails}
            disabled={loading}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted disabled:opacity-50 cursor-pointer"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        )}
      </div>

      {!isAuthenticated && (
        <AuthRequiredCard
          title="Sign in to view tournament details"
          description="Tournament details and team registrations require an authenticated CricArena account."
        />
      )}

      {isAuthenticated && loading && (
        <div className="space-y-4">
          <div className="h-40 animate-pulse rounded-2xl border border-border bg-surface p-6" />
          <div className="grid gap-4 lg:grid-cols-2">
            <div className="h-60 animate-pulse rounded-2xl border border-border bg-surface p-6" />
            <div className="h-60 animate-pulse rounded-2xl border border-border bg-surface p-6" />
          </div>
        </div>
      )}

      {isAuthenticated && !loading && error && (
        <div className="flex flex-col items-start justify-between gap-3 rounded-2xl border border-live-soft bg-live-soft/50 p-5 sm:flex-row sm:items-center">
          <div className="flex items-start gap-2.5 text-sm text-live">
            <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
            <div>
              <div className="font-bold">Could not load tournament</div>
              <div className="text-xs">{error}</div>
            </div>
          </div>
          <button
            type="button"
            onClick={loadDetails}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-bold text-ink hover:bg-surface-muted cursor-pointer"
          >
            <RefreshCw className="h-3.5 w-3.5" />
            Retry
          </button>
        </div>
      )}

      {isAuthenticated && !loading && !error && tournament && (
        <>
          {/* Tournament Info Card */}
          <section className="rounded-2xl border border-border bg-surface p-5 sm:p-6 shadow-[var(--shadow-card)]">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <StatusBadge status={tournament.status} label={tournament.status} />
                  <span className="inline-flex items-center gap-1 rounded-full bg-surface-mint px-2.5 py-0.5 text-xs font-bold text-brand-hover">
                    <Trophy className="h-3.5 w-3.5" />
                    {formatTournamentType(tournament.format)}
                  </span>
                  {tournament.collegeName && (
                    <span className="inline-flex items-center gap-1 rounded-full bg-surface-muted px-2.5 py-0.5 text-xs font-semibold text-ink-secondary">
                      {tournament.collegeName}
                    </span>
                  )}
                  {/* Registration Status Pill */}
                  <span
                    className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-bold ${
                      registrationStatus === 'OPEN'
                        ? 'bg-pitch-soft text-pitch border border-pitch-soft'
                        : registrationStatus === 'UPCOMING'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-live-soft text-live'
                    }`}
                  >
                    <Clock className="h-3 w-3" />
                    {registrationStatus === 'OPEN'
                      ? 'Registration Open'
                      : registrationStatus === 'UPCOMING'
                        ? 'Registration Upcoming'
                        : 'Registration Closed'}
                  </span>
                </div>

                <h1 className="mt-2.5 text-2xl font-extrabold tracking-tight text-ink">
                  {tournament.name}
                </h1>

                <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs font-medium text-ink-secondary">
                  <span className="inline-flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5 text-ink-muted" />
                    {tournament.startDate} to {tournament.endDate}
                  </span>
                  <span className="inline-flex items-center gap-1.5">
                    <Users className="h-3.5 w-3.5 text-ink-muted" />
                    {registeredTeams.length} Registered Team{registeredTeams.length === 1 ? '' : 's'}
                  </span>
                  {tournament.registrationDeadline && (
                    <span className="inline-flex items-center gap-1.5">
                      <Clock className="h-3.5 w-3.5 text-ink-muted" />
                      Deadline: {tournament.registrationDeadline.replace('T', ' ')}
                    </span>
                  )}
                </div>

                {tournament.description && (
                  <p className="mt-3 max-w-3xl text-sm text-ink-secondary leading-relaxed">
                    {tournament.description}
                  </p>
                )}
              </div>

              {/* Action buttons */}
              <div className="flex flex-wrap items-center gap-2 self-start">
                {!roleAssignment?.role && (
                  <button
                    type="button"
                    onClick={() => setApplyModalOpen(true)}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
                  >
                    <Trophy className="h-3.5 w-3.5" />
                    Apply as Captain
                  </button>
                )}

                {isCaptain && !isMyTeamRegistered && myActiveTeam && (
                  <button
                    type="button"
                    disabled={registrationClosed || busyTeamId === myActiveTeam.id}
                    onClick={() => handleRegisterTeam(myActiveTeam)}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover disabled:opacity-50 transition-colors cursor-pointer"
                  >
                    <Plus className="h-3.5 w-3.5" />
                    {busyTeamId === myActiveTeam.id ? 'Registering...' : 'Register My Team'}
                  </button>
                )}

                {isCaptain && isMyTeamRegistered && (
                  <button
                    type="button"
                    onClick={() => {
                      const myReg = registeredTeams.find((r) => String(r.teamId) === String(myTeamId));
                      setSquadTeam(myReg || { teamId: myTeamId, teamName: 'My Team' });
                      setSquadModalOpen(true);
                    }}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
                  >
                    <Users className="h-3.5 w-3.5" />
                    Manage Squad (15 Max)
                  </button>
                )}

                {canManage && tournament.status === 'DRAFT' && (
                  <button
                    type="button"
                    onClick={async () => {
                      try {
                        const published = await tournamentApi.publish(tournament.id);
                        setTournament(published);
                        setFeedback({
                          type: 'success',
                          text: `Tournament "${published.name}" published successfully! Status is now LIVE.`,
                        });
                      } catch (err) {
                        setFeedback({
                          type: 'error',
                          text: err.message || 'Failed to publish tournament.',
                        });
                      }
                    }}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-pitch px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-emerald-700 transition-colors cursor-pointer"
                  >
                    🚀 Publish Tournament
                  </button>
                )}

                {canManage && (
                  <button
                    type="button"
                    onClick={() => setEditOpen(true)}
                    className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-4 py-2 text-xs font-bold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
                  >
                    <Edit3 className="h-3.5 w-3.5" />
                    Edit Tournament
                  </button>
                )}
              </div>
            </div>
          </section>

          {/* Registration Feedback Banner */}
          {feedback && (
            <div
              role="status"
              className={`flex items-start gap-2.5 rounded-2xl border p-4 text-sm ${
                feedback.type === 'success'
                  ? 'border-pitch-soft bg-pitch-soft/60 text-pitch'
                  : 'border-live-soft bg-live-soft/60 text-live'
              }`}
            >
              {feedback.type === 'success' ? (
                <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0" />
              ) : (
                <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
              )}
              <span className="font-medium">{feedback.text}</span>
            </div>
          )}

          {/* Team Registration Management */}
          <section className="grid gap-5 lg:grid-cols-2">
            {/* Registered Teams */}
            <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)]">
              <div className="flex items-center justify-between border-b border-border pb-3">
                <div>
                  <h2 className="text-base font-extrabold text-ink">Registered Teams</h2>
                  <p className="text-xs text-ink-secondary">
                    Teams eligible to compete in {tournament.name}.
                  </p>
                </div>
                <span className="rounded-full bg-brand-soft px-2.5 py-0.5 text-xs font-bold text-brand-hover tabular-nums">
                  {registeredTeams.length}
                </span>
              </div>

              {registeredTeams.length === 0 ? (
                <div className="py-8 text-center text-sm text-ink-secondary">
                  No teams registered yet.
                </div>
              ) : (
                <div className="mt-3 space-y-2.5">
                  {registeredTeams.map((reg) => {
                    const team = reg.team;
                    const busy = busyTeamId === reg.teamId;
                    const isMyOwnTeam = String(reg.teamId) === String(myTeamId);
                    return (
                      <div
                        key={reg.id}
                        className={`flex items-center justify-between gap-3 rounded-xl border p-3 ${
                          isMyOwnTeam ? 'border-brand/40 bg-brand-soft/20' : 'border-border bg-canvas'
                        }`}
                      >
                        <div className="flex min-w-0 items-center gap-3">
                          <span
                            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-xs font-extrabold ${getTeamBadgeClass(
                              reg.teamName
                            )}`}
                          >
                            {team?.shortName?.slice(0, 3) || getTeamInitials(reg.teamName)}
                          </span>
                          <div className="min-w-0">
                            <div className="truncate text-sm font-bold text-ink flex items-center gap-1.5">
                              <span>{reg.teamName}</span>
                              {isMyOwnTeam && (
                                <span className="rounded-full bg-brand-soft px-2 py-0.2 text-[10px] font-bold text-brand-hover">
                                  Your Team
                                </span>
                              )}
                            </div>
                            <div className="flex flex-wrap items-center gap-x-2 text-xs text-ink-secondary">
                              {team?.city && (
                                <span className="inline-flex items-center gap-1">
                                  <MapPin className="h-3 w-3 text-ink-muted" />
                                  {team.city}
                                </span>
                              )}
                              {team?.collegeCode && <span>• {team.collegeCode}</span>}
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          {(isMyOwnTeam || canManage) && (
                            <button
                              type="button"
                              onClick={() => {
                                setSquadTeam(reg);
                                setSquadModalOpen(true);
                              }}
                              className="inline-flex items-center gap-1 rounded-lg border border-border bg-surface px-2.5 py-1.5 text-xs font-semibold text-brand hover:bg-surface-muted transition-colors cursor-pointer"
                            >
                              <Users className="h-3.5 w-3.5" />
                              <span>Squad</span>
                            </button>
                          )}
                          {canManage && (
                            <button
                              type="button"
                              disabled={busy}
                              onClick={() => handleRemoveTeam(reg)}
                              className="inline-flex items-center gap-1 rounded-lg border border-border bg-surface px-2.5 py-1.5 text-xs font-semibold text-live hover:bg-live-soft hover:border-live-soft disabled:opacity-50 transition-colors cursor-pointer"
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                              <span>{busy ? 'Removing...' : 'Remove'}</span>
                            </button>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Available Active Teams */}
            <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)]">
              <div className="flex items-center justify-between border-b border-border pb-3">
                <div>
                  <h2 className="text-base font-extrabold text-ink">Available Teams</h2>
                  <p className="text-xs text-ink-secondary">
                    Active teams not yet registered in this tournament.
                  </p>
                </div>
                <span className="rounded-full bg-surface-muted px-2.5 py-0.5 text-xs font-bold text-ink-secondary tabular-nums">
                  {availableTeams.length}
                </span>
              </div>

              {registrationClosed && (
                <div className="mt-3 rounded-xl bg-boundary-soft/60 px-3 py-2 text-xs font-medium text-boundary">
                  Registration is currently closed for this tournament.
                </div>
              )}

              <div className="relative mt-3">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
                <input
                  type="text"
                  value={teamSearch}
                  onChange={(e) => setTeamSearch(e.target.value)}
                  placeholder="Filter available teams..."
                  className="w-full rounded-xl border border-border bg-canvas py-2 pl-9 pr-3 text-xs text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
                />
              </div>

              {availableTeams.length === 0 ? (
                <div className="py-8 text-center text-sm text-ink-secondary">
                  {activeTeams.length === 0
                    ? 'No active teams exist yet.'
                    : 'All active teams matching your filter are already registered.'}
                </div>
              ) : (
                <div className="mt-3 max-h-96 space-y-2.5 overflow-y-auto pr-1">
                  {availableTeams.map((team) => {
                    const busy = busyTeamId === team.id;
                    const isMyOwnTeam = String(team.id) === String(myTeamId);
                    return (
                      <div
                        key={team.id}
                        className="flex items-center justify-between gap-3 rounded-xl border border-border bg-canvas p-3"
                      >
                        <div className="flex min-w-0 items-center gap-3">
                          <span
                            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-xl text-xs font-extrabold ${getTeamBadgeClass(
                              team.name
                            )}`}
                          >
                            {team.shortName?.slice(0, 3) || getTeamInitials(team.name)}
                          </span>
                          <div className="min-w-0">
                            <div className="truncate text-sm font-bold text-ink">{team.name}</div>
                            <div className="truncate text-xs text-ink-secondary">
                              {team.city || team.homeGround || 'Active Team'}
                              {team.collegeCode ? ` • ${team.collegeCode}` : ''}
                            </div>
                          </div>
                        </div>

                        {(canManage || isMyOwnTeam) && (
                          <button
                            type="button"
                            disabled={busy || registrationClosed}
                            onClick={() => handleRegisterTeam(team)}
                            className="inline-flex items-center gap-1 rounded-lg bg-brand px-3 py-1.5 text-xs font-bold text-white hover:bg-brand-hover disabled:opacity-50 transition-colors cursor-pointer"
                          >
                            <Plus className="h-3.5 w-3.5" />
                            <span>{busy ? 'Adding...' : isMyOwnTeam ? 'Register My Team' : 'Register'}</span>
                          </button>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </section>

          {/* Tournament Matches */}
          <section className="space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-extrabold text-ink">
                Tournament Matches ({matches.length})
              </h2>
            </div>

            {matches.length === 0 ? (
              <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-sm text-ink-secondary">
                No fixtures have been scheduled for this tournament yet.
              </div>
            ) : (
              <div className="grid gap-4 md:grid-cols-2">
                {matches.map((m) => (
                  <MatchCard
                    key={m.id}
                    match={m}
                    scorecard={scorecardsByMatchId[m.id]}
                    canManage={canManage}
                  />
                ))}
              </div>
            )}
          </section>
        </>
      )}

      {/* Edit Tournament Modal */}
      <TournamentFormModal
        open={editOpen}
        onClose={() => setEditOpen(false)}
        tournament={tournament}
        onSaved={(updated) => {
          setTournament(updated);
          setFeedback({
            type: 'success',
            text: `Tournament "${updated.name}" updated successfully.`,
          });
        }}
      />

      {/* Captain Application Modal */}
      <CaptainApplicationModal
        open={applyModalOpen}
        onClose={() => setApplyModalOpen(false)}
        defaultTournamentId={tournament?.id}
        defaultCollegeId={tournament?.collegeId}
        onSubmitted={() => {
          setFeedback({
            type: 'success',
            text: 'Captain application submitted! Review is pending by College Admin.',
          });
        }}
      />

      {/* Squad Management Modal */}
      {squadTeam && (
        <SquadManagementModal
          open={squadModalOpen}
          onClose={() => {
            setSquadModalOpen(false);
            setSquadTeam(null);
          }}
          tournamentId={tournament?.id}
          teamId={squadTeam.teamId}
          tournamentName={tournament?.name}
          teamName={squadTeam.teamName}
          onSquadSaved={() => {
            setFeedback({
              type: 'success',
              text: `Tournament squad updated for ${squadTeam.teamName}.`,
            });
          }}
        />
      )}
    </div>
  );
}
