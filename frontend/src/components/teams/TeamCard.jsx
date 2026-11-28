import { Link } from 'react-router-dom';
import {
  ArrowRight,
  Building2,
  Edit3,
  Mail,
  MapPin,
  PowerOff,
  ShieldAlert,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import {
  getTeamInitials,
  getTeamMonogramClass,
} from '../../utils/teamHelpers';

export default function TeamCard({ team, onEdit, onDeactivate, canManage }) {
  const initials = getTeamInitials(team.name, team.shortName);
  const monogramBg = getTeamMonogramClass(team.name);

  return (
    <div
      className={`group relative flex flex-col justify-between rounded-2xl border bg-surface p-5 transition-all duration-200 hover:shadow-md ${
        team.active
          ? 'border-border hover:border-brand/40 shadow-[var(--shadow-card)]'
          : 'border-border/70 bg-surface/80 opacity-90'
      }`}
    >
      <div>
        {/* Top bar: Monogram, Name & Status */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3.5 min-w-0">
            <div
              className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl text-base font-black shadow-xs ${monogramBg}`}
              aria-label={`${team.name} monogram`}
            >
              {initials}
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <Link
                  to={`/teams/${team.id}`}
                  className="truncate text-base font-extrabold text-ink group-hover:text-brand transition-colors"
                  title={team.name}
                >
                  {team.name}
                </Link>
                {team.shortName && (
                  <span className="shrink-0 rounded-md bg-canvas px-1.5 py-0.5 text-[10px] font-bold text-ink-secondary border border-border">
                    {team.shortName}
                  </span>
                )}
              </div>
              <p className="truncate text-xs text-ink-muted mt-0.5">
                Team #{team.id}
              </p>
            </div>
          </div>

          <div className="shrink-0">
            <StatusBadge status={team.active ? 'ACTIVE' : 'INACTIVE'} />
          </div>
        </div>

        {/* Metadata section */}
        <div className="mt-4 space-y-2 border-t border-border/60 pt-3 text-xs text-ink-secondary">
          <div className="flex items-center gap-2">
            <Building2 className="h-3.5 w-3.5 shrink-0 text-brand" />
            <span className="truncate font-medium text-ink">
              {team.collegeName || 'Unknown College'}
            </span>
            {team.collegeCode && (
              <span className="rounded bg-brand-soft px-1.5 py-0.5 text-[10px] font-bold text-brand-hover">
                {team.collegeCode}
              </span>
            )}
          </div>

          <div className="flex items-center gap-2">
            <MapPin className="h-3.5 w-3.5 shrink-0 text-ink-muted" />
            <span className="truncate">
              {team.city
                ? `${team.city}${team.homeGround ? ` • ${team.homeGround}` : ''}`
                : team.homeGround || 'Location not specified'}
            </span>
          </div>

          {team.contactEmail && (
            <div className="flex items-center gap-2">
              <Mail className="h-3.5 w-3.5 shrink-0 text-ink-muted" />
              <span className="truncate font-mono text-[11px] text-ink-muted">
                {team.contactEmail}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Card Actions Footer */}
      <div className="mt-5 flex items-center justify-between gap-2 border-t border-border/60 pt-3">
        <Link
          to={`/teams/${team.id}`}
          className="inline-flex items-center gap-1.5 rounded-xl bg-canvas px-3 py-1.5 text-xs font-bold text-ink-secondary hover:bg-brand-soft hover:text-brand-hover border border-border transition-colors"
        >
          <span>View Team</span>
          <ArrowRight className="h-3.5 w-3.5" />
        </Link>

        {canManage && (
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              onClick={() => onEdit?.(team)}
              className="inline-flex items-center gap-1 rounded-xl px-2.5 py-1.5 text-xs font-semibold text-ink-secondary hover:bg-surface-muted hover:text-ink transition-colors"
              title="Edit team details"
            >
              <Edit3 className="h-3.5 w-3.5" />
              <span>Edit</span>
            </button>

            {team.active && onDeactivate && (
              <button
                type="button"
                onClick={() => onDeactivate?.(team)}
                className="inline-flex items-center gap-1 rounded-xl px-2.5 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50 transition-colors"
                title="Deactivate team"
              >
                <PowerOff className="h-3.5 w-3.5" />
                <span>Deactivate</span>
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
