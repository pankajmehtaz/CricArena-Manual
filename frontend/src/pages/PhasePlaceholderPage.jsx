import { Link } from 'react-router-dom';
import { ArrowRight, CheckCircle2, Layers } from 'lucide-react';
import StatusBadge from '../components/common/StatusBadge';
import { useAuth } from '../context/AuthContext';

export default function PhasePlaceholderPage({ title, subtitle, phaseLabel }) {
  const { isAuthenticated } = useAuth();

  return (
    <div className="space-y-5">
      {/* Page Header */}
      <div className="flex flex-col gap-3 rounded-2xl border border-border bg-surface p-5 sm:flex-row sm:items-center sm:justify-between shadow-[var(--shadow-card)]">
        <div>
          <div className="inline-flex items-center gap-2 rounded-full bg-surface-mint px-3 py-1 text-xs font-bold text-brand-hover mb-2">
            <Layers className="h-3.5 w-3.5" />
            <span>Phase 1 Scaffold Active</span>
          </div>
          <h1 className="text-2xl font-extrabold tracking-tight text-ink">{title}</h1>
          <p className="mt-1 text-sm text-ink-secondary">{subtitle}</p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <StatusBadge status="IN_PROGRESS" />
          <StatusBadge status="COMPLETED" />
          <StatusBadge status="SCHEDULED" />
        </div>
      </div>

      {/* Design System & Integration Readiness Preview */}
      <div className="grid gap-4 md:grid-cols-2">
        <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)]">
          <h2 className="text-base font-bold text-ink">Light Theme & Palette Tokens</h2>
          <p className="mt-1 text-xs text-ink-secondary">
            High-contrast off-white canvas, pure white cards, cricket teal &amp; pitch green accents.
          </p>
          <div className="mt-4 grid grid-cols-2 gap-2.5 text-xs font-semibold">
            <div className="flex items-center gap-2.5 rounded-xl border border-border bg-canvas p-2.5">
              <span className="h-6 w-6 rounded-lg bg-brand shrink-0" />
              <div>
                <div className="text-ink">Cricket Teal</div>
                <div className="text-[11px] font-normal text-ink-muted">#0D9488</div>
              </div>
            </div>
            <div className="flex items-center gap-2.5 rounded-xl border border-border bg-canvas p-2.5">
              <span className="h-6 w-6 rounded-lg bg-pitch shrink-0" />
              <div>
                <div className="text-ink">Pitch Green</div>
                <div className="text-[11px] font-normal text-ink-muted">#15803D</div>
              </div>
            </div>
            <div className="flex items-center gap-2.5 rounded-xl border border-border bg-canvas p-2.5">
              <span className="h-6 w-6 rounded-lg bg-boundary shrink-0" />
              <div>
                <div className="text-ink">Boundary 4s</div>
                <div className="text-[11px] font-normal text-ink-muted">#D97706</div>
              </div>
            </div>
            <div className="flex items-center gap-2.5 rounded-xl border border-border bg-canvas p-2.5">
              <span className="h-6 w-6 rounded-lg bg-live shrink-0" />
              <div>
                <div className="text-ink">Live / Wicket</div>
                <div className="text-[11px] font-normal text-ink-muted">#DC2626</div>
              </div>
            </div>
          </div>
        </div>

        <div className="rounded-2xl border border-border bg-surface p-5 shadow-[var(--shadow-card)] flex flex-col justify-between">
          <div>
            <h2 className="text-base font-bold text-ink">Next Implementation Step</h2>
            <p className="mt-1 text-xs text-ink-secondary">
              {phaseLabel} will wire this view directly to the Spring Boot backend APIs.
            </p>
            <ul className="mt-3 space-y-2 text-xs text-ink-secondary">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-brand shrink-0" />
                <span>Vite dev server + `/api` proxy to `127.0.0.1:8080`</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-brand shrink-0" />
                <span>JWT `apiClient` + automatic 401 session handling</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-brand shrink-0" />
                <span>Responsive desktop navbar + mobile floating bottom bar</span>
              </li>
            </ul>
          </div>

          <div className="mt-5 flex items-center gap-3">
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white hover:bg-brand-hover transition-colors"
            >
              <span>{isAuthenticated ? 'View Account Session' : 'Test Sign In Screen'}</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
