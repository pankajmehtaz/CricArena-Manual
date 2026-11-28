import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Activity,
  AlertCircle,
  ArrowRight,
  Calendar,
  CheckCircle2,
  Plus,
  RefreshCw,
  Trophy,
} from 'lucide-react';
import MatchCard from '../components/matches/MatchCard';
import CreateMatchModal from '../components/matches/CreateMatchModal';
import PublicMatchAccessPanel from '../components/matches/PublicMatchAccessPanel';
import NormalUserDashboard from '../components/dashboard/NormalUserDashboard';
import CollegeAdminDashboard from '../components/dashboard/CollegeAdminDashboard';
import TeamCaptainDashboard from '../components/dashboard/TeamCaptainDashboard';
import PlatformAdminDashboard from '../components/dashboard/PlatformAdminDashboard';
import { useAuth } from '../context/AuthContext';
import { useMatchesWithScorecards } from '../hooks/useMatchesWithScorecards';
import { canManageMatches } from '../utils/matchHelpers';

function SectionHeader({ icon: Icon, iconClass, title, count, to, linkLabel = 'View all' }) {
  return (
    <div className="flex items-center justify-between gap-3">
      <div className="flex items-center gap-2">
        <span className={`inline-flex h-8 w-8 items-center justify-center rounded-xl ${iconClass}`}>
          <Icon className="h-4 w-4" />
        </span>
        <h2 className="text-lg font-extrabold tracking-tight text-ink">{title}</h2>
        {typeof count === 'number' && (
          <span className="rounded-full bg-surface-muted px-2.5 py-0.5 text-xs font-bold text-ink-secondary tabular-nums">
            {count}
          </span>
        )}
      </div>

      {to && (
        <Link
          to={to}
          className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover transition-colors"
        >
          <span>{linkLabel}</span>
          <ArrowRight className="h-3.5 w-3.5" />
        </Link>
      )}
    </div>
  );
}

function SkeletonCards() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      {[1, 2].map((n) => (
        <div
          key={n}
          className="h-52 animate-pulse rounded-2xl border border-border bg-surface p-5"
        >
          <div className="h-4 w-1/3 rounded bg-surface-muted" />
          <div className="mt-3 h-3 w-1/2 rounded bg-surface-muted" />
          <div className="mt-6 space-y-3">
            <div className="h-5 w-full rounded bg-surface-muted" />
            <div className="h-5 w-full rounded bg-surface-muted" />
          </div>
        </div>
      ))}
    </div>
  );
}

export default function HomePage() {
  const { isAuthenticated, user, roleAssignment } = useAuth();
  const showCreateMatch = isAuthenticated && canManageMatches(roleAssignment);
  const [createOpen, setCreateOpen] = useState(false);

  const { matches, scorecardsByMatchId, loading, error, reload } =
    useMatchesWithScorecards(isAuthenticated);

  const { liveMatches, upcomingMatches, recentCompleted } = useMemo(() => {
    const live = [];
    const upcoming = [];
    const completed = [];

    for (const m of matches) {
      if (m.status === 'IN_PROGRESS') live.push(m);
      else if (m.status === 'SCHEDULED') upcoming.push(m);
      else if (m.status === 'COMPLETED' || m.status === 'ABANDONED') completed.push(m);
    }

    upcoming.sort((a, b) => new Date(a.matchDateTime) - new Date(b.matchDateTime));
    completed.sort((a, b) => new Date(b.matchDateTime) - new Date(a.matchDateTime));

    return {
      liveMatches: live,
      upcomingMatches: upcoming.slice(0, 4),
      recentCompleted: completed.slice(0, 4),
    };
  }, [matches]);

  // If user is authenticated, render the dedicated role dashboard!
  if (isAuthenticated) {
    const role = roleAssignment?.role;
    if (role === 'PLATFORM_ADMIN') {
      return <PlatformAdminDashboard />;
    }
    if (role === 'COLLEGE_ADMIN') {
      return <CollegeAdminDashboard />;
    }
    if (role === 'TEAM_CAPTAIN') {
      return <TeamCaptainDashboard />;
    }
    return (
      <NormalUserDashboard
        matches={matches}
        scorecardsByMatchId={scorecardsByMatchId}
        onRefresh={reload}
      />
    );
  }

  // Public Guest View
  return (
    <div className="space-y-6">
      {/* Welcome / Header Hero Card */}
      <section className="rounded-2xl border border-border bg-surface p-5 sm:p-6 shadow-[var(--shadow-card)]">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-surface-mint px-3 py-1 text-xs font-bold text-brand-hover">
              <Activity className="h-3.5 w-3.5" />
              CricArena College Cricket Center
            </span>
            <h1 className="mt-2.5 text-2xl sm:text-3xl font-extrabold tracking-tight text-ink">
              Live Cricket Scores & Match Center
            </h1>
            <p className="mt-1 text-sm text-ink-secondary">
              Track live innings, browse upcoming fixtures, and inspect complete match scorecards.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Link
              to="/matches"
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-4 py-2.5 text-sm font-semibold text-ink hover:bg-surface-muted transition-colors"
            >
              <Activity className="h-4 w-4 text-brand" />
              <span>All Matches</span>
            </Link>
            <Link
              to="/tournaments"
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-4 py-2.5 text-sm font-semibold text-ink hover:bg-surface-muted transition-colors"
            >
              <Trophy className="h-4 w-4 text-boundary" />
              <span>Tournaments</span>
            </Link>
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2.5 text-sm font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
            >
              <span>Sign In / Sign Up</span>
            </Link>
          </div>
        </div>
      </section>

      {/* Quick Public Scorecard Lookup Panel */}
      <PublicMatchAccessPanel />

      {/* Main Matches Sections */}
      <div className="space-y-6">
        {/* Live Matches Section */}
        <section className="space-y-3">
          <SectionHeader
            icon={Activity}
            iconClass="bg-live-soft text-live"
            title="Live Matches"
            count={liveMatches.length}
            to="/matches?status=IN_PROGRESS"
          />

          {loading ? (
            <SkeletonCards />
          ) : liveMatches.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {liveMatches.map((match) => (
                <MatchCard
                  key={match.id}
                  match={match}
                  scorecard={scorecardsByMatchId[match.id]}
                  canManage={false}
                />
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-xs text-ink-muted">
              No live matches currently in progress.
            </div>
          )}
        </section>

        {/* Upcoming Fixtures Section */}
        <section className="space-y-3">
          <SectionHeader
            icon={Calendar}
            iconClass="bg-brand-soft text-brand-hover"
            title="Upcoming Matches"
            count={upcomingMatches.length}
            to="/matches?status=SCHEDULED"
          />

          {loading ? (
            <SkeletonCards />
          ) : upcomingMatches.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {upcomingMatches.map((match) => (
                <MatchCard
                  key={match.id}
                  match={match}
                  scorecard={scorecardsByMatchId[match.id]}
                  canManage={false}
                />
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-xs text-ink-muted">
              No upcoming matches scheduled at this time.
            </div>
          )}
        </section>

        {/* Recently Completed Section */}
        <section className="space-y-3">
          <SectionHeader
            icon={CheckCircle2}
            iconClass="bg-pitch-soft text-pitch"
            title="Recent Results"
            count={recentCompleted.length}
            to="/matches?status=COMPLETED"
          />

          {loading ? (
            <SkeletonCards />
          ) : recentCompleted.length > 0 ? (
            <div className="grid gap-4 md:grid-cols-2">
              {recentCompleted.map((match) => (
                <MatchCard
                  key={match.id}
                  match={match}
                  scorecard={scorecardsByMatchId[match.id]}
                  canManage={false}
                />
              ))}
            </div>
          ) : (
            <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-xs text-ink-muted">
              No completed matches found.
            </div>
          )}
        </section>
      </div>

      <CreateMatchModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => reload()}
      />
    </div>
  );
}
