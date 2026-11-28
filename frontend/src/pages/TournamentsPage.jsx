import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  ArrowRight,
  Calendar,
  Edit3,
  Plus,
  RefreshCw,
  Search,
  Trophy,
  X,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import StatusBadge from '../components/common/StatusBadge';
import TournamentFormModal, {
  formatTournamentType,
} from '../components/tournaments/TournamentFormModal';
import { useAuth } from '../context/AuthContext';
import { useCollege } from '../context/CollegeContext';
import { tournamentApi } from '../services/apiClient';
import { canManageMatches } from '../utils/matchHelpers';

const STATUS_FILTERS = [
  { key: 'ALL', label: 'All' },
  { key: 'DRAFT', label: 'Draft' },
  { key: 'SCHEDULED', label: 'Scheduled' },
  { key: 'PUBLISHED', label: 'Published' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];

export default function TournamentsPage() {
  const { isAuthenticated, roleAssignment } = useAuth();
  const { selectedCollegeId } = useCollege();
  const canManage = isAuthenticated && canManageMatches(roleAssignment);
  const isCaptain = roleAssignment?.role === 'TEAM_CAPTAIN';

  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingTournament, setEditingTournament] = useState(null);

  const loadTournaments = useCallback(async () => {
    if (!isAuthenticated) {
      setTournaments([]);
      setLoading(false);
      setError('');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const list = await tournamentApi.getAll();
      setTournaments(Array.isArray(list) ? list : []);
    } catch (err) {
      setError(err.message || 'Failed to load tournaments.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    loadTournaments();
  }, [loadTournaments]);

  const scopedTournaments = useMemo(() => {
    return tournaments
      .filter((t) => !selectedCollegeId || !t.collegeId || String(t.collegeId) === String(selectedCollegeId))
      .filter((t) => canManage || t.status !== 'DRAFT');
  }, [tournaments, selectedCollegeId, canManage]);

  const visibleStatusFilters = useMemo(
    () => STATUS_FILTERS.filter((f) => canManage || f.key !== 'DRAFT'),
    [canManage]
  );

  const countsByStatus = useMemo(() => {
    const counts = {
      ALL: scopedTournaments.length,
      DRAFT: 0,
      SCHEDULED: 0,
      PUBLISHED: 0,
      COMPLETED: 0,
      CANCELLED: 0,
    };
    for (const t of scopedTournaments) {
      if (counts[t.status] !== undefined) {
        counts[t.status] += 1;
      }
    }
    return counts;
  }, [scopedTournaments]);

  const filteredTournaments = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return scopedTournaments
      .filter((t) => statusFilter === 'ALL' || t.status === statusFilter)
      .filter((t) => {
        if (!q) return true;
        return (
          t.name?.toLowerCase().includes(q) ||
          t.description?.toLowerCase().includes(q) ||
          t.format?.toLowerCase().includes(q)
        );
      })
      .sort((a, b) => new Date(b.startDate) - new Date(a.startDate));
  }, [scopedTournaments, statusFilter, searchQuery]);

  const openCreateModal = () => {
    setEditingTournament(null);
    setModalOpen(true);
  };

  const openEditModal = (t) => {
    setEditingTournament(t);
    setModalOpen(true);
  };

  return (
    <div className="space-y-5">
      {/* Page Heading */}
      <div className="flex flex-col gap-3 rounded-2xl border border-border bg-surface p-5 sm:flex-row sm:items-center sm:justify-between shadow-[var(--shadow-card)]">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-ink">Tournaments</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Organize competitions, manage schedules, and register participating teams.
          </p>
        </div>

        {isAuthenticated && (
          <div className="flex items-center gap-2.5">
            <button
              type="button"
              onClick={loadTournaments}
              disabled={loading}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted disabled:opacity-50 transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            {canManage && (
              <button
                type="button"
                onClick={openCreateModal}
                className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors cursor-pointer"
              >
                <Plus className="h-4 w-4" />
                Create Tournament
              </button>
            )}
          </div>
        )}
      </div>

      {/* Auth Guard */}
      {!isAuthenticated && (
        <AuthRequiredCard
          title="Sign in to view and manage tournaments"
          description="Tournament listings, creation, and team registrations require an authenticated CricArena account."
        />
      )}

      {isAuthenticated && (
        <>
          {/* Search + Status Filters */}
          <div className="space-y-3 rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
            <div className="relative">
              <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-muted" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search tournaments by name, description, or format..."
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

            <div className="flex flex-wrap items-center gap-2" role="tablist" aria-label="Tournament status filters">
              {visibleStatusFilters.map((filter) => {
                const isActive = statusFilter === filter.key;
                return (
                  <button
                    key={filter.key}
                    type="button"
                    role="tab"
                    aria-selected={isActive}
                    onClick={() => setStatusFilter(filter.key)}
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
                      {countsByStatus[filter.key]}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Loading State */}
          {loading && (
            <div className="grid gap-4 md:grid-cols-2">
              {[1, 2, 3, 4].map((n) => (
                <div
                  key={n}
                  className="h-44 animate-pulse rounded-2xl border border-border bg-surface p-5"
                >
                  <div className="h-5 w-1/2 rounded bg-surface-muted" />
                  <div className="mt-3 h-3 w-1/3 rounded bg-surface-muted" />
                  <div className="mt-6 h-4 w-full rounded bg-surface-muted" />
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
                  <div className="font-bold">Could not load tournaments</div>
                  <div className="text-xs">{error}</div>
                </div>
              </div>
              <button
                type="button"
                onClick={loadTournaments}
                className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-bold text-ink hover:bg-surface-muted cursor-pointer"
              >
                <RefreshCw className="h-3.5 w-3.5" />
                Retry
              </button>
            </div>
          )}

          {/* Empty State */}
          {!loading && !error && filteredTournaments.length === 0 && (
            <div className="rounded-2xl border border-dashed border-border bg-surface p-10 text-center">
              <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-surface-muted text-ink-muted">
                <Trophy className="h-6 w-6" />
              </div>
              <p className="text-base font-bold text-ink">
                {searchQuery || statusFilter !== 'ALL'
                  ? 'No matching tournaments found'
                  : 'No tournaments created yet'}
              </p>
              <p className="mx-auto mt-1 max-w-md text-sm text-ink-secondary">
                {searchQuery || statusFilter !== 'ALL'
                  ? 'Try adjusting your search query or selecting another status filter.'
                  : 'Host your own college cricket tournament to register teams and schedule matches.'}
              </p>
              <div className="mt-5 flex justify-center gap-2.5">
                {(searchQuery || statusFilter !== 'ALL') && (
                  <button
                    type="button"
                    onClick={() => {
                      setSearchQuery('');
                      setStatusFilter('ALL');
                    }}
                    className="rounded-xl border border-border bg-canvas px-4 py-2 text-xs font-bold text-ink hover:bg-surface-muted cursor-pointer"
                  >
                    Reset Filters
                  </button>
                )}
                {canManage && (
                  <button
                    type="button"
                    onClick={openCreateModal}
                    className="inline-flex items-center gap-1.5 rounded-full bg-brand px-5 py-2.5 text-sm font-bold text-white hover:bg-brand-hover transition-colors cursor-pointer"
                  >
                    <Plus className="h-4 w-4" />
                    Create Tournament
                  </button>
                )}
              </div>
            </div>
          )}

          {/* Tournament Cards Grid */}
          {!loading && !error && filteredTournaments.length > 0 && (
            <div className="grid gap-4 md:grid-cols-2">
              {filteredTournaments.map((t) => (
                <article
                  key={t.id}
                  className="flex flex-col justify-between rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] hover:border-brand/40 transition-all"
                >
                  <div>
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <span className="inline-flex items-center gap-1 rounded-full bg-surface-mint px-2.5 py-0.5 text-[11px] font-bold text-brand-hover">
                          <Trophy className="h-3 w-3" />
                          {formatTournamentType(t.format)}
                        </span>
                        <h2 className="mt-2 truncate text-lg font-extrabold text-ink">
                          {t.name}
                        </h2>
                      </div>
                      <StatusBadge status={t.status} label={t.status} />
                    </div>

                    <div className="mt-2 flex items-center gap-1.5 text-xs font-medium text-ink-secondary">
                      <Calendar className="h-3.5 w-3.5 text-ink-muted" />
                      <span>
                        {t.startDate} to {t.endDate}
                      </span>
                    </div>

                    <p className="mt-3 line-clamp-2 text-sm text-ink-secondary">
                      {t.description || 'No description provided.'}
                    </p>
                  </div>

                  <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-border pt-3.5">
                    {canManage ? (
                      <button
                        type="button"
                        onClick={() => openEditModal(t)}
                        className="inline-flex items-center gap-1.5 rounded-full border border-border bg-canvas px-3 py-1.5 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
                      >
                        <Edit3 className="h-3.5 w-3.5" />
                        Edit
                      </button>
                    ) : (
                      <span />
                    )}

                    <Link
                      to={`/tournaments/${t.id}`}
                      className="inline-flex items-center gap-1.5 rounded-full border border-brand-soft bg-surface-mint px-3.5 py-1.5 text-xs font-bold text-brand-hover hover:bg-brand-soft transition-colors"
                    >
                      <span>
                        {canManage ? 'Manage & Teams' : isCaptain ? 'Details & Squad' : 'View Tournament'}
                      </span>
                      <ArrowRight className="h-3.5 w-3.5" />
                    </Link>
                  </div>
                </article>
              ))}
            </div>
          )}
        </>
      )}

      <TournamentFormModal
        open={modalOpen}
        tournament={editingTournament}
        onClose={() => setModalOpen(false)}
        onSaved={() => loadTournaments()}
      />
    </div>
  );
}
