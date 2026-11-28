import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { AlertCircle, Plus, RefreshCw, Search, X } from 'lucide-react';
import MatchCard from '../components/matches/MatchCard';
import CreateMatchModal from '../components/matches/CreateMatchModal';
import PublicMatchAccessPanel from '../components/matches/PublicMatchAccessPanel';
import { useAuth } from '../context/AuthContext';
import { useCollege } from '../context/CollegeContext';
import { useMatchesWithScorecards } from '../hooks/useMatchesWithScorecards';
import { canManageMatches } from '../utils/matchHelpers';

const STATUS_FILTERS = [
  { key: 'ALL', label: 'All' },
  { key: 'IN_PROGRESS', label: 'Live' },
  { key: 'SCHEDULED', label: 'Upcoming' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'ABANDONED', label: 'Abandoned' },
];

export default function MatchesPage() {
  const { isAuthenticated, roleAssignment } = useAuth();
  const { selectedCollegeId } = useCollege();
  const showCreateMatch = isAuthenticated && canManageMatches(roleAssignment);
  const [searchParams, setSearchParams] = useSearchParams();
  const initialStatus = searchParams.get('status');
  const activeFilter = STATUS_FILTERS.some((f) => f.key === initialStatus)
    ? initialStatus
    : 'ALL';

  const [searchQuery, setSearchQuery] = useState('');
  const [createOpen, setCreateOpen] = useState(false);

  const { matches, scorecardsByMatchId, loading, error, reload } =
    useMatchesWithScorecards(isAuthenticated, selectedCollegeId);

  const countsByFilter = useMemo(() => {
    const counts = {
      ALL: matches.length,
      IN_PROGRESS: 0,
      SCHEDULED: 0,
      COMPLETED: 0,
      ABANDONED: 0,
    };
    for (const m of matches) {
      if (counts[m.status] !== undefined) {
        counts[m.status] += 1;
      }
    }
    return counts;
  }, [matches]);

  const filteredMatches = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return matches
      .filter((m) => activeFilter === 'ALL' || m.status === activeFilter)
      .filter((m) => {
        if (!q) return true;
        return (
          m.teamAName?.toLowerCase().includes(q) ||
          m.teamBName?.toLowerCase().includes(q) ||
          m.tournamentName?.toLowerCase().includes(q) ||
          m.venue?.toLowerCase().includes(q) ||
          m.format?.toLowerCase().includes(q)
        );
      })
      .sort((a, b) => new Date(b.matchDateTime) - new Date(a.matchDateTime));
  }, [matches, activeFilter, searchQuery]);

  const handleSelectFilter = (key) => {
    if (key === 'ALL') {
      setSearchParams({});
    } else {
      setSearchParams({ status: key });
    }
  };

  return (
    <div className="space-y-5">
      {/* Page Heading */}
      <div className="flex flex-col gap-3 rounded-2xl border border-border bg-surface p-5 sm:flex-row sm:items-center sm:justify-between shadow-[var(--shadow-card)]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-ink">Matches</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Browse live fixtures, upcoming schedules, completed results, and scorecards.
          </p>
        </div>

        {isAuthenticated && (
          <div className="flex items-center gap-2.5">
            <button
              type="button"
              onClick={reload}
              disabled={loading}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted disabled:opacity-50 transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            {showCreateMatch && (
              <button
                type="button"
                onClick={() => setCreateOpen(true)}
                className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
              >
                <Plus className="h-4 w-4" />
                Create Match
              </button>
            )}
          </div>
        )}
      </div>

      {/* Public Users Guard */}
      {!isAuthenticated && <PublicMatchAccessPanel />}

      {/* Authenticated Controls & Listing */}
      {isAuthenticated && (
        <>
          {/* Search + Status Filter Bar */}
          <div className="space-y-3 rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by team, tournament, venue, or format..."
                className="w-full rounded-xl border border-border bg-canvas py-2.5 pl-10 pr-9 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none focus:ring-2 focus:ring-brand/15"
              />
              {searchQuery && (
                <button
                  type="button"
                  onClick={() => setSearchQuery('')}
                  aria-label="Clear search"
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-ink-muted hover:text-ink cursor-pointer"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>

            <div className="flex flex-wrap items-center gap-2" role="tablist" aria-label="Match status filters">
              {STATUS_FILTERS.map((filter) => {
                const isActive = activeFilter === filter.key;
                return (
                  <button
                    key={filter.key}
                    type="button"
                    role="tab"
                    aria-selected={isActive}
                    onClick={() => handleSelectFilter(filter.key)}
                    className={`inline-flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-xs font-semibold transition-colors cursor-pointer ${
                      isActive
                        ? 'bg-brand text-white shadow-xs'
                        : 'border border-border bg-canvas text-ink-secondary hover:bg-surface-muted hover:text-ink'
                    }`}
                  >
                    <span>{filter.label}</span>
                    <span
                      className={`rounded-full px-1.5 py-0.2 text-[10px] font-bold tabular-nums ${
                        isActive ? 'bg-white/20 text-white' : 'bg-surface-muted text-ink-secondary'
                      }`}
                    >
                      {countsByFilter[filter.key]}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="grid gap-4 lg:grid-cols-2">
              {[1, 2, 3, 4].map((n) => (
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
          )}

          {/* Error State */}
          {!loading && error && (
            <div className="flex flex-col items-start justify-between gap-3 rounded-2xl border border-live-soft bg-live-soft/50 p-5 sm:flex-row sm:items-center">
              <div className="flex items-start gap-2.5 text-sm text-live">
                <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
                <div>
                  <div className="font-bold">Failed to fetch matches</div>
                  <div className="text-xs">{error}</div>
                </div>
              </div>
              <button
                type="button"
                onClick={reload}
                className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-bold text-ink hover:bg-surface-muted cursor-pointer"
              >
                <RefreshCw className="h-3.5 w-3.5" />
                Try Again
              </button>
            </div>
          )}

          {/* Empty State */}
          {!loading && !error && filteredMatches.length === 0 && (
            <div className="rounded-2xl border border-dashed border-border bg-surface p-10 text-center">
              <p className="text-base font-bold text-ink">No matching matches found</p>
              <p className="mt-1 text-sm text-ink-secondary">
                {searchQuery || activeFilter !== 'ALL'
                  ? 'Try clearing your search text or selecting another status filter.'
                  : 'No matches have been created yet.'}
              </p>
              {(searchQuery || activeFilter !== 'ALL') && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchQuery('');
                    handleSelectFilter('ALL');
                  }}
                  className="mt-4 rounded-xl border border-border bg-canvas px-4 py-2 text-xs font-bold text-ink hover:bg-surface-muted cursor-pointer"
                >
                  Reset Filters
                </button>
              )}
            </div>
          )}

          {/* Matches Grid */}
          {!loading && !error && filteredMatches.length > 0 && (
            <div className="grid gap-4 lg:grid-cols-2">
              {filteredMatches.map((match) => (
                <MatchCard
                  key={match.id}
                  match={match}
                  scorecard={scorecardsByMatchId[match.id]}
                  showScorerShortcut={showCreateMatch}
                />
              ))}
            </div>
          )}
        </>
      )}

      <CreateMatchModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => reload()}
      />
    </div>
  );
}
