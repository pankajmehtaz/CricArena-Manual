import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  Building2,
  CheckCircle2,
  Filter,
  Loader2,
  Plus,
  PowerOff,
  RefreshCw,
  Search,
  ShieldCheck,
  Users,
  X,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import TeamCard from '../components/teams/TeamCard';
import TeamFormModal from '../components/teams/TeamFormModal';
import { useAuth } from '../context/AuthContext';
import { useCollege } from '../context/CollegeContext';
import { captainApplicationApi, collegeApi, teamApi } from '../services/apiClient';
import { canManageTeams } from '../utils/teamHelpers';

const STATUS_TABS = [
  { key: 'ALL', label: 'All Teams' },
  { key: 'ACTIVE', label: 'Active' },
  { key: 'INACTIVE', label: 'Inactive' },
];

export default function TeamsPage() {
  const { isAuthenticated, roleAssignment, refreshSession } = useAuth();
  const { selectedCollegeId, setSelectedCollegeId } = useCollege();
  const canManage = isAuthenticated && canManageTeams(roleAssignment);

  const [teams, setTeams] = useState([]);
  const [colleges, setColleges] = useState([]);
  const [hasApprovedApp, setHasApprovedApp] = useState(false);
  const canCreate = canManage || hasApprovedApp;
  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState(null); // { type: 'success' | 'error', text }

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [collegeFilter, setCollegeFilter] = useState('ALL');

  // Modals & Actions
  const [formModalOpen, setFormModalOpen] = useState(false);
  const [editingTeam, setEditingTeam] = useState(null);
  const [deactivatingTeam, setDeactivatingTeam] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const loadData = useCallback(async () => {
    if (!isAuthenticated) {
      setTeams([]);
      setColleges([]);
      setHasApprovedApp(false);
      setLoading(false);
      setError('');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const [teamList, collegeList, apps] = await Promise.all([
        teamApi.getAll(),
        collegeApi.getActive().catch(() => []),
        captainApplicationApi.getMyApplications().catch(() => []),
      ]);
      setTeams(Array.isArray(teamList) ? teamList : []);
      setColleges(Array.isArray(collegeList) ? collegeList : []);
      setHasApprovedApp(Array.isArray(apps) && apps.some((a) => a.status === 'APPROVED' && a.canCreateTeam));
    } catch (err) {
      setError(err.message || 'Failed to load teams.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  // Keep collegeFilter in sync with roleAssignment.collegeId or header selectedCollegeId
  useEffect(() => {
    if (roleAssignment?.collegeId) {
      setCollegeFilter(String(roleAssignment.collegeId));
    } else if (selectedCollegeId) {
      setCollegeFilter(String(selectedCollegeId));
    } else {
      setCollegeFilter('ALL');
    }
  }, [roleAssignment, selectedCollegeId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Statistics based on active scoped/selected college filter
  const stats = useMemo(() => {
    const activeCollegeId = roleAssignment?.collegeId
      ? String(roleAssignment.collegeId)
      : collegeFilter !== 'ALL'
        ? collegeFilter
        : selectedCollegeId;
    const relevantTeams =
      activeCollegeId && activeCollegeId !== 'ALL'
        ? teams.filter((t) => String(t.collegeId) === String(activeCollegeId))
        : teams;

    const total = relevantTeams.length;
    let active = 0;
    let inactive = 0;
    const collegeSet = new Set();

    for (const t of relevantTeams) {
      if (t.active) active += 1;
      else inactive += 1;
      if (t.collegeId) collegeSet.add(t.collegeId);
    }

    return {
      total,
      active,
      inactive,
      collegesCount: collegeSet.size,
    };
  }, [teams, roleAssignment, selectedCollegeId]);

  // Filtered teams
  const filteredTeams = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    const activeCollegeId = roleAssignment?.collegeId ? String(roleAssignment.collegeId) : (collegeFilter !== 'ALL' ? collegeFilter : selectedCollegeId);

    return teams
      .filter((t) => {
        if (!activeCollegeId || activeCollegeId === 'ALL') return true;
        return String(t.collegeId) === String(activeCollegeId);
      })
      .filter((t) => {
        if (statusFilter === 'ACTIVE') return t.active;
        if (statusFilter === 'INACTIVE') return !t.active;
        return true;
      })
      .filter((t) => {
        if (!q) return true;
        return (
          t.name?.toLowerCase().includes(q) ||
          t.shortName?.toLowerCase().includes(q) ||
          t.city?.toLowerCase().includes(q) ||
          t.collegeName?.toLowerCase().includes(q) ||
          t.homeGround?.toLowerCase().includes(q)
        );
      })
      .sort((a, b) => {
        // Active first, then alphabetically
        if (a.active !== b.active) return a.active ? -1 : 1;
        return a.name.localeCompare(b.name);
      });
  }, [teams, statusFilter, collegeFilter, searchQuery, roleAssignment, selectedCollegeId]);

  const handleCreateOpen = () => {
    setEditingTeam(null);
    setFormModalOpen(true);
  };

  const handleEditOpen = (team) => {
    setEditingTeam(team);
    setFormModalOpen(true);
  };

  const handleSaved = async (savedTeam, isEdit) => {
    setTeams((prev) => {
      if (isEdit) {
        return prev.map((t) => (t.id === savedTeam.id ? savedTeam : t));
      }
      return [savedTeam, ...prev];
    });
    setFeedback({
      type: 'success',
      text: isEdit
        ? `Team "${savedTeam.name}" updated successfully.`
        : `Team "${savedTeam.name}" created successfully. You are now the official Team Captain.`,
    });
    await refreshSession();
    loadData();
    setTimeout(() => setFeedback(null), 4000);
  };

  const handleConfirmDeactivate = async () => {
    if (!deactivatingTeam) return;
    setActionLoading(true);
    try {
      await teamApi.deactivate(deactivatingTeam.id);
      setTeams((prev) =>
        prev.map((t) => (t.id === deactivatingTeam.id ? { ...t, active: false } : t))
      );
      setFeedback({
        type: 'success',
        text: `Team "${deactivatingTeam.name}" has been deactivated.`,
      });
      setTimeout(() => setFeedback(null), 4000);
      setDeactivatingTeam(null);
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
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-ink">Teams</h1>
          <p className="mt-1 text-sm text-ink-secondary">
            Manage college teams, rosters, and player profiles.
          </p>
        </div>
        <AuthRequiredCard
          title="Sign in to view and manage Teams"
          description="CricArena team management and roster coordination require authentication. Sign in with your college or platform administrator credentials to view registered teams."
        />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Heading & Actions */}
      <div className="flex flex-col gap-4 rounded-3xl border border-border bg-surface p-5 sm:p-6 shadow-[var(--shadow-card)] sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <span className="flex h-7 w-7 items-center justify-center rounded-lg bg-brand-soft text-brand-hover">
              <Users className="h-4 w-4" />
            </span>
            <h1 className="text-2xl font-extrabold tracking-tight text-ink">Teams</h1>
          </div>
          <p className="mt-1 text-sm text-ink-secondary">
            Explore registered college cricket teams, home grounds, and squads.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={loadData}
            disabled={loading}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-xs font-bold text-ink-secondary hover:bg-surface hover:text-ink transition-colors disabled:opacity-50"
            title="Refresh team list"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin text-brand' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>

          {canCreate && (
            <button
              type="button"
              onClick={handleCreateOpen}
              className="inline-flex items-center gap-2 rounded-xl bg-brand px-4 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-brand-hover transition-colors"
            >
              <Plus className="h-4 w-4" />
              <span>Create Team</span>
            </button>
          )}
        </div>
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
            className="text-ink-muted hover:text-ink"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Summary Statistics Bar */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 sm:gap-4">
        <div className="rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
          <p className="text-[11px] font-bold uppercase tracking-wider text-ink-muted">Total Teams</p>
          <p className="mt-1 text-2xl font-black text-ink">{stats.total}</p>
        </div>
        <div className="rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
          <p className="text-[11px] font-bold uppercase tracking-wider text-pitch">Active Teams</p>
          <p className="mt-1 text-2xl font-black text-pitch">{stats.active}</p>
        </div>
        <div className="rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
          <p className="text-[11px] font-bold uppercase tracking-wider text-ink-muted">Inactive</p>
          <p className="mt-1 text-2xl font-black text-ink-secondary">{stats.inactive}</p>
        </div>
        <div className="rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)]">
          <p className="text-[11px] font-bold uppercase tracking-wider text-brand">Colleges</p>
          <p className="mt-1 text-2xl font-black text-brand">{stats.collegesCount}</p>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="flex flex-col gap-3 rounded-2xl border border-border bg-surface p-4 shadow-[var(--shadow-card)] md:flex-row md:items-center md:justify-between">
        {/* Status Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0">
          {STATUS_TABS.map((tab) => {
            const count =
              tab.key === 'ALL'
                ? stats.total
                : tab.key === 'ACTIVE'
                  ? stats.active
                  : stats.inactive;
            const isSelected = statusFilter === tab.key;
            return (
              <button
                key={tab.key}
                type="button"
                onClick={() => setStatusFilter(tab.key)}
                className={`inline-flex shrink-0 items-center gap-1.5 rounded-xl px-3 py-1.5 text-xs font-bold transition-colors ${
                  isSelected
                    ? 'bg-brand text-white shadow-xs'
                    : 'bg-canvas text-ink-secondary hover:bg-surface-muted hover:text-ink'
                }`}
              >
                <span>{tab.label}</span>
                <span
                  className={`rounded-md px-1.5 py-0.2 text-[10px] font-extrabold ${
                    isSelected ? 'bg-white/25 text-white' : 'bg-surface text-ink-muted border border-border'
                  }`}
                >
                  {count}
                </span>
              </button>
            );
          })}
        </div>

        {/* Search Input & Optional College Dropdown */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5">
          {/* College Filter - Only show for PLATFORM_ADMIN if they want to browse all institutions */}
          {roleAssignment?.role === 'PLATFORM_ADMIN' && (
            <div className="flex items-center gap-2 shrink-0 rounded-xl border border-border bg-canvas px-3 py-2 focus-within:border-brand focus-within:bg-surface transition-colors">
              <Filter className="h-3.5 w-3.5 shrink-0 text-ink-muted" />
              <select
                value={collegeFilter}
                onChange={(e) => {
                  const next = e.target.value;
                  setCollegeFilter(next);
                  setSelectedCollegeId(next === 'ALL' ? '' : next);
                }}
                className="w-full sm:w-auto bg-transparent text-xs font-medium text-ink outline-none cursor-pointer"
              >
                <option value="ALL">All Colleges</option>
                {colleges.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} {c.code ? `(${c.code})` : ''}
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Search Box */}
          <div className="relative min-w-[220px]">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-ink-muted" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search team, city, ground..."
              className="w-full rounded-xl border border-border bg-canvas py-2 pl-9 pr-8 text-xs font-medium text-ink outline-none transition-colors focus:border-brand focus:bg-surface"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-ink-muted hover:text-ink"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div
              key={i}
              className="h-44 rounded-2xl border border-border bg-surface p-5 animate-pulse shadow-[var(--shadow-card)]"
            >
              <div className="flex items-center gap-3">
                <div className="h-12 w-12 rounded-2xl bg-canvas" />
                <div className="space-y-2 flex-1">
                  <div className="h-4 w-3/4 rounded bg-canvas" />
                  <div className="h-3 w-1/3 rounded bg-canvas" />
                </div>
              </div>
              <div className="mt-5 space-y-2">
                <div className="h-3 w-5/6 rounded bg-canvas" />
                <div className="h-3 w-1/2 rounded bg-canvas" />
              </div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-rose-200 bg-rose-50 p-8 text-center text-rose-800">
          <AlertCircle className="h-10 w-10 text-rose-600 mb-2" />
          <p className="text-base font-extrabold">{error}</p>
          <p className="mt-1 text-xs text-rose-700">Check server connection and try again.</p>
          <button
            type="button"
            onClick={loadData}
            className="mt-4 rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white hover:bg-rose-700 transition-colors"
          >
            Retry Loading
          </button>
        </div>
      ) : filteredTeams.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-3xl border border-dashed border-border bg-surface p-12 text-center shadow-[var(--shadow-card)]">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-soft text-brand mb-3">
            <Users className="h-7 w-7" />
          </div>
          <h3 className="text-lg font-extrabold text-ink">No Teams Found</h3>
          <p className="mt-1 max-w-sm text-xs text-ink-secondary">
            {searchQuery || statusFilter !== 'ALL' || collegeFilter !== 'ALL'
              ? 'No teams match your current search and filter criteria. Try clearing some filters.'
              : 'There are no registered cricket teams yet. Get started by creating your first team.'}
          </p>

          <div className="mt-5 flex items-center gap-3">
            {(searchQuery || statusFilter !== 'ALL' || collegeFilter !== 'ALL') && (
              <button
                type="button"
                onClick={() => {
                  setSearchQuery('');
                  setStatusFilter('ALL');
                  setCollegeFilter('ALL');
                }}
                className="rounded-xl border border-border px-3.5 py-2 text-xs font-bold text-ink-secondary hover:bg-surface-muted transition-colors"
              >
                Clear Filters
              </button>
            )}
            {canCreate && (
              <button
                type="button"
                onClick={handleCreateOpen}
                className="inline-flex items-center gap-2 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white hover:bg-brand-hover transition-colors"
              >
                <Plus className="h-3.5 w-3.5" />
                <span>Create Team</span>
              </button>
            )}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredTeams.map((team) => (
            <TeamCard
              key={team.id}
              team={team}
              onEdit={handleEditOpen}
              onDeactivate={(t) => setDeactivatingTeam(t)}
              canManage={canManage && canManageTeams(roleAssignment, team)}
            />
          ))}
        </div>
      )}

      {/* Team Create/Edit Modal */}
      <TeamFormModal
        open={formModalOpen}
        onClose={() => setFormModalOpen(false)}
        onSaved={handleSaved}
        team={editingTeam}
        colleges={colleges}
        defaultCollegeId={roleAssignment?.collegeId || null}
      />

      {/* Deactivate Confirmation Modal */}
      {deactivatingTeam && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-ink/40 backdrop-blur-xs animate-in fade-in duration-150"
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
                  <span className="font-bold text-ink">{deactivatingTeam.name}</span>?
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
                onClick={() => setDeactivatingTeam(null)}
                disabled={actionLoading}
                className="rounded-xl border border-border px-4 py-2 text-xs font-semibold text-ink-secondary hover:bg-surface-muted transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmDeactivate}
                disabled={actionLoading}
                className="inline-flex items-center gap-1.5 rounded-xl bg-rose-600 px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-rose-700 transition-colors disabled:opacity-50"
              >
                {actionLoading ? (
                  <>
                    <Loader2 className="h-3.5 w-3.5 animate-spin" />
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
