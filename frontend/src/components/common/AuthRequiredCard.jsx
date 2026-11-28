import { Link, useLocation } from 'react-router-dom';
import { Lock, LogIn } from 'lucide-react';

export default function AuthRequiredCard({ title, description }) {
  const location = useLocation();

  return (
    <div className="rounded-2xl border border-border bg-surface p-6 sm:p-8 shadow-[var(--shadow-card)] max-w-xl">
      <div className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-soft text-brand-hover mb-3">
        <Lock className="h-5 w-5" />
      </div>
      <h2 className="text-lg font-extrabold text-ink">{title}</h2>
      <p className="mt-1.5 text-sm text-ink-secondary leading-relaxed">{description}</p>
      <div className="mt-5">
        <Link
          to="/login"
          state={{ from: location.pathname + location.search }}
          className="inline-flex items-center gap-2 rounded-xl bg-brand px-4 py-2.5 text-sm font-bold text-white shadow-xs hover:bg-brand-hover transition-colors"
        >
          <LogIn className="h-4 w-4" />
          <span>Sign In</span>
        </Link>
      </div>
    </div>
  );
}
