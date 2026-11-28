import { Link } from 'react-router-dom';
import { Calendar, ClipboardList, MapPin, Play, Trophy } from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import {
  formatMatchDateTime,
  getMatchCardFootnote,
  getTeamBadgeClass,
  getTeamInitials,
  getTeamRowsForCard,
} from '../../utils/matchHelpers';

export default function MatchCard({ match, scorecard, showScorerShortcut = false }) {
  const teamRows = getTeamRowsForCard(match, scorecard);
  const winnerTeamId = scorecard?.result?.winnerTeamId ?? match.winnerTeamId;
  const footnote = getMatchCardFootnote(match, scorecard);

  return (
    <article className="group flex flex-col justify-between rounded-2xl border border-border bg-surface p-4 sm:p-5 shadow-[var(--shadow-card)] hover:border-brand/40 transition-all">
      <div>
        {/* Top Row: Tournament + Format & Status Badge */}
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-brand-hover">
              <Trophy className="h-3.5 w-3.5 shrink-0" />
              <span className="truncate">{match.tournamentName || 'Tournament Match'}</span>
            </div>
            <div className="mt-1 flex flex-wrap items-center gap-x-2.5 gap-y-1 text-xs text-ink-secondary">
              <span className="inline-flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5 text-ink-muted" />
                {formatMatchDateTime(match.matchDateTime)}
              </span>
              <span className="text-ink-muted">•</span>
              <span>
                {match.format} ({match.maxOvers} Ov)
              </span>
            </div>
          </div>

          <StatusBadge status={match.status} />
        </div>

        {/* Venue */}
        <div className="mt-1.5 flex items-center gap-1 text-xs text-ink-secondary">
          <MapPin className="h-3.5 w-3.5 shrink-0 text-ink-muted" />
          <span className="truncate">{match.venue}</span>
        </div>

        {/* Team Score Rows */}
        <div className="mt-4 space-y-2.5">
          {teamRows.map((row) => {
            const isWinner = winnerTeamId != null && winnerTeamId === row.teamId;
            const isLiveBatting =
              match.status === 'IN_PROGRESS' && row.innings?.status === 'IN_PROGRESS';
            const emphasize = isWinner || isLiveBatting;

            return (
              <div key={row.teamId} className="flex items-center justify-between gap-3">
                <div className="flex min-w-0 items-center gap-2.5">
                  <span
                    className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-xl text-xs font-extrabold tracking-tight shadow-xs ${getTeamBadgeClass(
                      row.teamName
                    )}`}
                  >
                    {getTeamInitials(row.teamName)}
                  </span>
                  <span
                    className={`truncate text-sm ${
                      emphasize ? 'font-bold text-ink' : 'font-medium text-ink-secondary'
                    }`}
                  >
                    {row.teamName}
                  </span>
                </div>

                <div className="shrink-0 text-right tabular-nums">
                  {row.innings ? (
                    <div className="flex items-baseline gap-1.5">
                      <span
                        className={`text-base ${
                          emphasize ? 'font-extrabold text-ink' : 'font-semibold text-ink-secondary'
                        }`}
                      >
                        {row.innings.totalRuns}/{row.innings.wickets}
                      </span>
                      <span className="text-xs text-ink-muted">
                        ({row.innings.overs} Ov)
                      </span>
                    </div>
                  ) : (
                    <span className="text-xs font-medium text-ink-muted">
                      {match.status === 'IN_PROGRESS' ? 'Yet to bat' : '—'}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Footnote & Quick Actions */}
      <div className="mt-4 border-t border-border pt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p
          className={`text-xs font-medium ${
            match.status === 'COMPLETED'
              ? 'text-pitch font-semibold'
              : match.status === 'IN_PROGRESS'
                ? 'text-ink'
                : 'text-ink-secondary'
          }`}
        >
          {footnote}
        </p>

        <div className="flex flex-wrap items-center gap-2 shrink-0">
          <Link
            to={`/matches/${match.id}`}
            className="inline-flex items-center gap-1 rounded-full border border-border bg-canvas px-3 py-1.5 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors"
          >
            Details
          </Link>
          <Link
            to={`/matches/${match.id}/scorecard`}
            className="inline-flex items-center gap-1 rounded-full border border-brand-soft bg-surface-mint px-3 py-1.5 text-xs font-semibold text-brand-hover hover:bg-brand-soft transition-colors"
          >
            <ClipboardList className="h-3.5 w-3.5" />
            Scorecard
          </Link>
          {showScorerShortcut && match.status === 'SCHEDULED' && (
            <Link
              to={`/matches/${match.id}/setup`}
              className="inline-flex items-center gap-1 rounded-full bg-brand px-3 py-1.5 text-xs font-semibold text-white hover:bg-brand-hover transition-colors"
            >
              Setup
            </Link>
          )}
          {showScorerShortcut && match.status === 'IN_PROGRESS' && (
            <Link
              to={`/matches/${match.id}/score`}
              className="inline-flex items-center gap-1 rounded-full bg-live px-3 py-1.5 text-xs font-semibold text-white hover:opacity-90 transition-opacity"
            >
              <Play className="h-3 w-3 fill-current" />
              Score
            </Link>
          )}
        </div>
      </div>
    </article>
  );
}
