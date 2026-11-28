import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, ClipboardList, Lock, LogIn } from 'lucide-react';

export default function PublicMatchAccessPanel() {
  const [matchIdInput, setMatchIdInput] = useState('');
  const navigate = useNavigate();

  const handleLookup = (e) => {
    e.preventDefault();
    const cleaned = matchIdInput.trim();
    if (!cleaned || !/^\d+$/.test(cleaned)) return;
    navigate(`/matches/${cleaned}/scorecard`);
  };

  return (
    <div className="grid gap-4 md:grid-cols-2">
      <div className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)] flex flex-col justify-between">
        <div>
          <div className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover mb-3">
            <Lock className="h-5 w-5" />
          </div>
          <h2 className="text-lg font-extrabold text-ink">Sign in to view all matches</h2>
          <p className="mt-1.5 text-sm text-ink-secondary leading-relaxed">
            Full match listings, tournament schedules, squad setup, and live ball-by-ball scoring
            require an authenticated CricArena account.
          </p>
        </div>

        <div className="mt-5">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 rounded-xl bg-brand px-4 py-2.5 text-sm font-bold text-white shadow-xs hover:bg-brand-hover transition-colors"
          >
            <LogIn className="h-4 w-4" />
            <span>Sign In</span>
          </Link>
        </div>
      </div>

      <div className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)] flex flex-col justify-between">
        <div>
          <div className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-pitch-soft text-pitch mb-3">
            <ClipboardList className="h-5 w-5" />
          </div>
          <h2 className="text-lg font-extrabold text-ink">Open Public Match Scorecard</h2>
          <p className="mt-1.5 text-sm text-ink-secondary leading-relaxed">
            Scorecards are publicly accessible without signing in. Enter a Match ID to open its
            live or final scorecard directly.
          </p>
        </div>

        <form onSubmit={handleLookup} className="mt-5 flex gap-2">
          <input
            type="number"
            min={1}
            value={matchIdInput}
            onChange={(e) => setMatchIdInput(e.target.value)}
            placeholder="Enter Match ID (e.g. 1)"
            className="flex-1 rounded-xl border border-border bg-canvas px-3.5 py-2 text-sm text-ink placeholder:text-ink-muted focus:border-brand focus:bg-surface focus:outline-none"
          />
          <button
            type="submit"
            disabled={!matchIdInput.trim()}
            className="inline-flex items-center gap-1.5 rounded-xl border border-brand-soft bg-surface-mint px-4 py-2 text-sm font-bold text-brand-hover hover:bg-brand-soft disabled:opacity-50 transition-colors cursor-pointer"
          >
            <span>View</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </form>
      </div>
    </div>
  );
}
