import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  ArrowRight,
  Calendar,
  CheckCircle2,
  Clock,
  Globe,
  Loader2,
  Plus,
  RefreshCw,
  ShieldCheck,
  Trophy,
  Users,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import MatchCard from '../matches/MatchCard';
import CaptainReviewModal from '../captain/CaptainReviewModal';
import CreateMatchModal from '../matches/CreateMatchModal';
import TournamentFormModal from '../tournaments/TournamentFormModal';
import { useAuth } from '../../context/AuthContext';
import { useCollege } from '../../context/CollegeContext';
import { captainApplicationApi, matchApi, teamApi, tournamentApi } from '../../services/apiClient';

export default function CollegeAdminDashboard() {
  const { roleAssignment } = useAuth();
  const { selectedCollege } = useCollege();
  const collegeId = roleAssignment?.collegeId;

  const [tournaments, setTournaments] = useState([]);
  const [pendingApplications, setPendingApplications] = useState([]);
  const [teams, setTeams] = useState([]);
  const [collegeMatches, setCollegeMatches] = useState([]);
  const [scorecardsByMatchId, setScorecardsByMatchId] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [reviewApp, setReviewApp] = useState(null);
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [createTournamentOpen, setCreateTournamentOpen] = useState(false);
  const [createMatchOpen, setCreateMatchOpen] = useState(false);
  const [publishingId, setPublishingId] = useState(null);

  const loadCollegeData = async () => {
    if (!collegeId) return;
    setLoading(true);
    setError('');
    try {
      const [tourneyList, appList, teamList, allMatches] = await Promise.all([
        tournamentApi.getAll().catch(() => []),
        captainApplicationApi.getByCollege(collegeId, 'PENDING').catch(() => []),
        teamApi.getByCollege(collegeId).catch(() => []),
        matchApi.getAll().catch(() => []),
      ]);

      const myTourneys = (Array.isArray(tourneyList) ? tourneyList : []).filter(
        (t) => String(t.collegeId) === String(collegeId)
      );
      const myTourneyIds = new Set(myTourneys.map((t) => String(t.id)));
      const myMatches = (Array.isArray(allMatches) ? allMatches : [])
        .filter((m) => m.tournamentId && myTourneyIds.has(String(m.tournamentId)))
        .sort((a, b) => new Date(b.matchDateTime) - new Date(a.matchDateTime));

      setTournaments(myTourneys);
      setPendingApplications(Array.isArray(appList) ? appList : []);
      setTeams(Array.isArray(teamList) ? teamList : []);
      setCollegeMatches(myMatches);

      const scorecardTargets = myMatches.filter(
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
      setError(err.message || 'Failed to load college admin data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCollegeData();
  }, [collegeId]);

  const handlePublishTournament = async (tournament) => {
    setPublishingId(tournament.id);
    setError('');
    try {
      await tournamentApi.publish(tournament.id);
      setFeedback(`Tournament "${tournament.name}" is now published and open for applications.`);
      loadCollegeData();
    } catch (err) {
      setError(err.message || 'Failed to publish tournament.');
    } finally {
      setPublishingId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 rounded-full bg-brand-soft px-3 py-0.5 text-xs font-bold text-brand-hover mb-2">
              <ShieldCheck className="h-3.5 w-3.5" />
              College Administrator Portal
            </div>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">
              {selectedCollege ? selectedCollege.name : 'College Administration'}
            </h1>
            {selectedCollege && (
              <p className="mt-1 text-sm text-ink-secondary">
                {[selectedCollege.city, selectedCollege.state].filter(Boolean).join(', ')}
                {selectedCollege.code ? ` • ${selectedCollege.code}` : ''}
              </p>
            )}
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <button
              type="button"
              onClick={loadCollegeData}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            <button
              type="button"
              onClick={() => setCreateMatchOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-xl border border-brand bg-brand-soft px-4 py-2 text-xs font-bold text-brand-hover shadow-xs hover:bg-surface-mint transition-colors cursor-pointer"
            >
              <Calendar className="h-3.5 w-3.5" />
              Schedule Match
            </button>
            <button
              type="button"
              onClick={() => setCreateTournamentOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
            >
              <Plus className="h-3.5 w-3.5" />
              Create Tournament
            </button>
          </div>
        </div>

        {/* Quick Metrics */}
        <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">Tournaments</span>
            <p className="mt-1 text-2xl font-black text-ink">{tournaments.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">Pending Applications</span>
            <p className="mt-1 text-2xl font-black text-brand-hover">{pendingApplications.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">Registered Teams</span>
            <p className="mt-1 text-2xl font-black text-ink">{teams.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-canvas p-4 text-center sm:text-left">
            <span className="text-xs font-semibold text-ink-secondary">College Matches</span>
            <p className="mt-1 text-2xl font-black text-ink">{collegeMatches.length}</p>
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

      {/* Captain Applications Review Queue */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-800">
              <Clock className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Captain Application Review Queue</h2>
              <p className="text-xs text-ink-secondary">
                {pendingApplications.length} candidate(s) awaiting your decision
              </p>
            </div>
          </div>
        </div>

        <div className="mt-4">
          {pendingApplications.length === 0 ? (
            <div className="py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No pending Captain applications to review.
            </div>
          ) : (
            <div className="space-y-3">
              {pendingApplications.map((app) => (
                <div
                  key={app.id}
                  className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 rounded-xl border border-border bg-canvas p-4 text-xs"
                >
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-bold text-ink text-sm">
                        {app.userName || app.userFullName || 'Applicant'}
                      </span>
                      <span className="text-ink-secondary">({app.userEmail})</span>
                      <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800">
                        PENDING
                      </span>
                    </div>
                    <p className="text-ink-secondary">
                      Tournament:{' '}
                      <span className="font-semibold text-brand-hover">{app.tournamentName}</span> •
                      Phone: {app.contactNumber}
                    </p>
                    <p className="text-ink pt-1 italic">"{app.reason}"</p>
                  </div>

                  <button
                    type="button"
                    onClick={() => {
                      setReviewApp(app);
                      setReviewModalOpen(true);
                    }}
                    className="self-start sm:self-center shrink-0 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
                  >
                    Review Application
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* College Tournaments Section */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">College Tournaments</h2>
              <p className="text-xs text-ink-secondary">Oversee tournament schedules and squad limits</p>
            </div>
          </div>
          <Link
            to="/tournaments"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>All Tournaments</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          {tournaments.length === 0 ? (
            <div className="sm:col-span-2 py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No tournaments created yet for this college. Click "Create Tournament" to get started.
            </div>
          ) : (
            tournaments.map((t) => (
              <div
                key={t.id}
                className="flex flex-col justify-between rounded-xl border border-border bg-canvas p-4 text-xs"
              >
                <div>
                  <div className="flex items-center justify-between gap-2">
                    <h3 className="font-bold text-ink text-sm">{t.name}</h3>
                    <StatusBadge status={t.status} />
                  </div>
                  <p className="mt-1 text-ink-secondary">{t.description || 'College tournament'}</p>
                  <div className="mt-2 text-[11px] text-ink-muted space-y-0.5">
                    <div>
                      Format: <span className="font-semibold text-ink">{t.format}</span>
                    </div>
                    <div>
                      Window: {t.startDate} to {t.endDate}
                    </div>
                    {t.registrationDeadline && (
                      <div>
                        Reg Deadline:{' '}
                        {new Date(t.registrationDeadline).toLocaleString('en-IN', {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </div>
                    )}
                  </div>
                </div>

                <div className="mt-4 flex items-center justify-end gap-2 pt-3 border-t border-border/60">
                  {t.status === 'DRAFT' && (
                    <button
                      type="button"
                      onClick={() => handlePublishTournament(t)}
                      disabled={publishingId === t.id}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-pitch px-3 py-1.5 font-bold text-white hover:bg-pitch-hover transition-colors cursor-pointer disabled:opacity-60"
                    >
                      {publishingId === t.id ? (
                        <Loader2 className="h-3.5 w-3.5 animate-spin" />
                      ) : (
                        <Globe className="h-3.5 w-3.5" />
                      )}
                      {publishingId === t.id ? 'Publishing...' : 'Publish'}
                    </button>
                  )}
                  <Link
                    to={`/tournaments/${t.id}`}
                    className="rounded-xl border border-border bg-surface px-3 py-1.5 font-bold text-brand hover:bg-surface-muted transition-colors"
                  >
                    Manage Tournament →
                  </Link>
                </div>
              </div>
            ))
          )}
        </div>
      </section>

      {/* College Matches Overview */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-surface-mint text-brand">
              <Calendar className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">College Matches &amp; Live Scoring</h2>
              <p className="text-xs text-ink-secondary">
                Setup Playing XI, launch ball-by-ball scoring, and review scorecards
              </p>
            </div>
          </div>
          <Link
            to="/matches"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>All Matches</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4">
          {collegeMatches.length === 0 ? (
            <div className="py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No matches scheduled for this college yet. Click "Schedule Match" above to create a fixture.
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {collegeMatches.slice(0, 4).map((m) => (
                <MatchCard
                  key={m.id}
                  match={m}
                  scorecard={scorecardsByMatchId[m.id]}
                  showScorerShortcut={true}
                />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* College Teams Register */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">College Teams</h2>
              <p className="text-xs text-ink-secondary">Cricket clubs affiliated with your institution</p>
            </div>
          </div>
          <Link
            to="/teams"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>All Teams</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4">
          {teams.length === 0 ? (
            <div className="py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No teams registered yet. Once captain applications are approved, captains can create their college teams.
            </div>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-3">
              {teams.map((tm) => (
                <Link
                  key={tm.id}
                  to={`/teams/${tm.id}`}
                  className="rounded-xl border border-border bg-canvas p-3.5 text-xs space-y-1.5 hover:border-brand/40 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-ink text-sm truncate">{tm.name}</span>
                    <span className="rounded bg-surface-muted px-2 py-0.5 text-[10px] font-extrabold text-brand-hover">
                      {tm.shortName}
                    </span>
                  </div>
                  <p className="text-ink-secondary truncate">
                    {tm.homeGround || tm.city || 'Campus Ground'}
                  </p>
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Review Modal */}
      <CaptainReviewModal
        open={reviewModalOpen}
        onClose={() => setReviewModalOpen(false)}
        application={reviewApp}
        onReviewed={(res) => {
          setFeedback(`Captain application #${res.id} review recorded.`);
          loadCollegeData();
        }}
      />

      {/* Tournament Create Modal */}
      <TournamentFormModal
        open={createTournamentOpen}
        onClose={() => setCreateTournamentOpen(false)}
        onSaved={() => {
          setFeedback('New tournament created successfully.');
          loadCollegeData();
        }}
      />

      {/* Schedule Match Modal */}
      <CreateMatchModal
        open={createMatchOpen}
        onClose={() => setCreateMatchOpen(false)}
        onCreated={() => {
          setFeedback('New match fixture scheduled successfully.');
          loadCollegeData();
        }}
      />
    </div>
  );
}
