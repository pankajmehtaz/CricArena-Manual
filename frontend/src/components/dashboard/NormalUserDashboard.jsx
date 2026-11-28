import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Activity,
  AlertCircle,
  ArrowRight,
  Building2,
  Calendar,
  CheckCircle2,
  Clock,
  Compass,
  FileText,
  Plus,
  RefreshCw,
  Trophy,
  Users,
  XCircle,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import MatchCard from '../matches/MatchCard';
import CaptainApplicationModal from '../captain/CaptainApplicationModal';
import TeamFormModal from '../teams/TeamFormModal';
import { useAuth } from '../../context/AuthContext';
import { useCollege } from '../../context/CollegeContext';
import { captainApplicationApi, tournamentApi } from '../../services/apiClient';

export default function NormalUserDashboard({ matches = [], scorecardsByMatchId = {}, onRefresh }) {
  const { user, refreshSession } = useAuth();
  const { colleges, selectedCollegeId, selectedCollege, setSelectedCollegeId, isCollegeLocked } =
    useCollege();

  const [myApplications, setMyApplications] = useState([]);
  const [collegeTournaments, setCollegeTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [appModalOpen, setAppModalOpen] = useState(false);
  const [selectedTournamentForApp, setSelectedTournamentForApp] = useState(null);
  const [createTeamModalOpen, setCreateTeamModalOpen] = useState(false);

  const loadUserData = async () => {
    setLoading(true);
    setError('');
    try {
      const [apps, allTourneys] = await Promise.all([
        captainApplicationApi.getMyApplications().catch(() => []),
        tournamentApi.getAll().catch(() => []),
      ]);
      setMyApplications(Array.isArray(apps) ? apps : []);

      const safeTourneys = (Array.isArray(allTourneys) ? allTourneys : []).filter(
        (t) => t.status !== 'DRAFT'
      );
      if (selectedCollegeId) {
        setCollegeTournaments(
          safeTourneys.filter(
            (t) => !t.collegeId || String(t.collegeId) === String(selectedCollegeId)
          )
        );
      } else {
        setCollegeTournaments(safeTourneys);
      }
    } catch (err) {
      setError(err.message || 'Failed to load user portal data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserData();
  }, [selectedCollegeId]);

  const latestPendingOrActiveApp = useMemo(() => {
    if (myApplications.length === 0) return null;
    const pending = myApplications.find((a) => a.status === 'PENDING');
    if (pending) return pending;
    return myApplications[0];
  }, [myApplications]);

  const handleWithdraw = async (appId) => {
    try {
      await captainApplicationApi.withdraw(appId);
      setFeedback('Captain application withdrawn.');
      loadUserData();
      setTimeout(() => setFeedback(''), 4000);
    } catch (err) {
      setError(err.message || 'Failed to withdraw application.');
    }
  };

  // Filter matches for selected college's tournaments
  const filteredMatches = useMemo(() => {
    if (!selectedCollegeId) return matches.slice(0, 4);
    const tourneyIds = new Set(collegeTournaments.map((t) => String(t.id)));
    return matches
      .filter((m) => !m.tournamentId || tourneyIds.has(String(m.tournamentId)))
      .slice(0, 4);
  }, [matches, selectedCollegeId, collegeTournaments]);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 rounded-full bg-brand-soft px-3 py-0.5 text-xs font-bold text-brand-hover mb-2">
              <Compass className="h-3.5 w-3.5" />
              Member Portal
            </div>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">
              Welcome back, {user?.fullName || 'Cricket Fan'}
            </h1>
            <p className="mt-1 text-sm text-ink-secondary">
              {selectedCollege ? (
                <>
                  Viewing tournaments and live action for{' '}
                  <span className="font-semibold text-ink">
                    {selectedCollege.name} ({selectedCollege.code})
                  </span>
                </>
              ) : (
                'Select your college below to filter campus tournaments and matches'
              )}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5 self-start sm:self-center">
            {!isCollegeLocked && (
              <div className="inline-flex items-center gap-2 rounded-xl border border-border bg-canvas px-3 py-2 text-xs font-semibold text-ink">
                <Building2 className="h-3.5 w-3.5 text-brand shrink-0" />
                <select
                  value={selectedCollegeId || ''}
                  onChange={(e) => setSelectedCollegeId(e.target.value)}
                  aria-label="Select your college"
                  className="bg-transparent text-xs font-semibold text-ink focus:outline-none cursor-pointer max-w-[200px] truncate"
                >
                  <option value="">Select your college</option>
                  {colleges.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name} ({c.code})
                    </option>
                  ))}
                </select>
              </div>
            )}
            <button
              type="button"
              onClick={() => {
                loadUserData();
                onRefresh?.();
              }}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>
        </div>
      </section>

      {feedback && (
        <div className="flex items-start gap-2.5 rounded-xl border border-pitch-soft bg-pitch-soft/70 p-3.5 text-xs text-pitch font-semibold">
          <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{feedback}</span>
        </div>
      )}

      {error && (
        <div className="flex items-start gap-2.5 rounded-xl border border-live-soft bg-live-soft/70 p-3.5 text-xs text-live">
          <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Become a Captain / Application Status Card */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-pitch-soft text-pitch">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Team Captaincy Status</h2>
              <p className="text-xs text-ink-secondary">Lead and register a college squad</p>
            </div>
          </div>
          {!latestPendingOrActiveApp || latestPendingOrActiveApp.status === 'REJECTED' || latestPendingOrActiveApp.status === 'WITHDRAWN' ? (
            <button
              type="button"
              onClick={() => {
                setSelectedTournamentForApp(null);
                setAppModalOpen(true);
              }}
              className="inline-flex items-center gap-1.5 rounded-full bg-brand px-4 py-1.5 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
            >
              <Plus className="h-3.5 w-3.5" />
              Become a Captain
            </button>
          ) : latestPendingOrActiveApp?.status === 'APPROVED' && latestPendingOrActiveApp.canCreateTeam ? (
            <button
              type="button"
              onClick={() => setCreateTeamModalOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-full bg-pitch px-4 py-1.5 text-xs font-bold text-white shadow-sm hover:bg-pitch-hover transition-colors cursor-pointer"
            >
              <Plus className="h-3.5 w-3.5" />
              Create Team
            </button>
          ) : null}
        </div>

        <div className="mt-4">
          {latestPendingOrActiveApp ? (
            <div className="rounded-xl border border-border bg-canvas p-4 text-xs">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-ink text-sm">
                      {latestPendingOrActiveApp.tournamentName || 'Tournament'}
                    </span>
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold ${
                        latestPendingOrActiveApp.status === 'PENDING'
                          ? 'bg-amber-100 text-amber-800 border border-amber-200'
                          : latestPendingOrActiveApp.status === 'APPROVED'
                            ? 'bg-pitch-soft text-pitch border border-pitch-soft'
                            : 'bg-live-soft text-live border border-live-soft'
                      }`}
                    >
                      {latestPendingOrActiveApp.status}
                    </span>
                  </div>
                  <p className="text-ink-secondary">
                    Application ID #{latestPendingOrActiveApp.id} • College: {latestPendingOrActiveApp.collegeName}
                  </p>
                  <p className="text-ink-secondary italic pt-1">
                    "{latestPendingOrActiveApp.reason}"
                  </p>
                  {latestPendingOrActiveApp.status === 'REJECTED' && latestPendingOrActiveApp.rejectionReason && (
                    <div className="mt-2 rounded-lg bg-live-soft/50 p-2 text-live">
                      <span className="font-bold">Rejection feedback: </span>
                      {latestPendingOrActiveApp.rejectionReason}
                    </div>
                  )}
                  {latestPendingOrActiveApp.status === 'APPROVED' && latestPendingOrActiveApp.canCreateTeam && (
                    <p className="text-pitch font-semibold pt-1">
                      🎉 Application approved by College Admin! You can now create your official cricket team below.
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-2 sm:self-center">
                  {latestPendingOrActiveApp.status === 'APPROVED' && latestPendingOrActiveApp.canCreateTeam && (
                    <button
                      type="button"
                      onClick={() => setCreateTeamModalOpen(true)}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-pitch px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-pitch-hover transition-colors cursor-pointer"
                    >
                      <Plus className="h-3.5 w-3.5" />
                      Create Your Team
                    </button>
                  )}
                  {latestPendingOrActiveApp.status === 'APPROVED' && latestPendingOrActiveApp.ownedTeamId && (
                    <Link
                      to={`/teams/${latestPendingOrActiveApp.ownedTeamId}`}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
                    >
                      <Users className="h-3.5 w-3.5" />
                      Manage Squad
                    </Link>
                  )}
                  {latestPendingOrActiveApp.status === 'PENDING' && (
                    <button
                      type="button"
                      onClick={() => handleWithdraw(latestPendingOrActiveApp.id)}
                      className="rounded-xl border border-border bg-surface px-3 py-1.5 text-xs font-semibold text-live hover:bg-live-soft transition-colors cursor-pointer"
                    >
                      Withdraw Application
                    </button>
                  )}
                  {latestPendingOrActiveApp.status === 'REJECTED' && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedTournamentForApp(null);
                        setAppModalOpen(true);
                      }}
                      className="rounded-xl bg-brand px-3 py-1.5 text-xs font-bold text-white hover:bg-brand-hover transition-colors cursor-pointer"
                    >
                      Re-Apply Now
                    </button>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-border bg-canvas/60 p-5 text-center sm:text-left">
              <p className="text-sm font-bold text-ink">No active captaincy applications</p>
              <p className="mt-1 text-xs text-ink-secondary">
                Ready to lead your college team? Click "Become a Captain" above to apply for a tournament and manage your squad.
              </p>
            </div>
          )}
        </div>
      </section>

      {/* Tournaments for Selected College */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Published Tournaments</h2>
              <p className="text-xs text-ink-secondary">
                {selectedCollege ? `Held at ${selectedCollege.name}` : 'Tournaments across all colleges'}
              </p>
            </div>
          </div>
          <Link
            to="/tournaments"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>View All</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          {collegeTournaments.length === 0 ? (
            <div className="col-span-2 py-8 text-center text-xs text-ink-muted">
              No published tournaments available for this college.
            </div>
          ) : (
            collegeTournaments.slice(0, 4).map((t) => (
              <div
                key={t.id}
                className="flex flex-col justify-between rounded-xl border border-border bg-canvas p-4 text-xs transition-shadow hover:shadow-sm"
              >
                <div>
                  <div className="flex items-center justify-between gap-2">
                    <h3 className="font-bold text-ink text-sm">{t.name}</h3>
                    <StatusBadge status={t.status} />
                  </div>
                  <p className="mt-1 text-ink-secondary line-clamp-2">
                    {t.description || 'College cricket tournament'}
                  </p>
                  <div className="mt-3 flex items-center gap-3 text-[11px] text-ink-muted">
                    <span>Format: {t.format}</span>
                    <span>•</span>
                    <span>Starts: {t.startDate}</span>
                  </div>
                </div>

                <div className="mt-4 flex items-center justify-between pt-3 border-t border-border/60">
                  <Link
                    to={`/tournaments/${t.id}`}
                    className="font-semibold text-brand hover:underline"
                  >
                    Tournament Details →
                  </Link>
                  {(!latestPendingOrActiveApp || latestPendingOrActiveApp.status !== 'PENDING') && (
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedTournamentForApp(t.id);
                        setAppModalOpen(true);
                      }}
                      className="rounded-full bg-surface border border-border px-2.5 py-1 text-[11px] font-bold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
                    >
                      Apply as Captain
                    </button>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </section>

      {/* Live & Upcoming Matches */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="inline-flex h-8 w-8 items-center justify-center rounded-xl bg-live-soft text-live">
              <Activity className="h-4 w-4" />
            </span>
            <h2 className="text-lg font-extrabold tracking-tight text-ink">Featured Matches</h2>
          </div>
          <Link
            to="/matches"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>All Matches</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        {filteredMatches.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-xs text-ink-muted">
            No matches scheduled or active right now.
          </div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2">
            {filteredMatches.map((m) => (
              <MatchCard
                key={m.id}
                match={m}
                scorecard={scorecardsByMatchId[m.id]}
                canManage={false}
              />
            ))}
          </div>
        )}
      </section>

      {/* Modals */}
      <CaptainApplicationModal
        open={appModalOpen}
        onClose={() => setAppModalOpen(false)}
        defaultTournamentId={selectedTournamentForApp}
        defaultCollegeId={selectedCollegeId}
        tournaments={collegeTournaments}
        onSubmitted={() => {
          setFeedback('Captain application submitted successfully! Review pending by College Admin.');
          loadUserData();
        }}
      />

      <TeamFormModal
        open={createTeamModalOpen}
        onClose={() => setCreateTeamModalOpen(false)}
        defaultCollegeId={latestPendingOrActiveApp?.collegeId || selectedCollegeId}
        onSaved={async (savedTeam) => {
          setFeedback(`Team "${savedTeam.name}" created successfully! You are now the official Team Captain.`);
          await refreshSession();
          loadUserData();
          onRefresh?.();
        }}
      />
    </div>
  );
}
