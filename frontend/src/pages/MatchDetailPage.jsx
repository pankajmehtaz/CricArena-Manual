import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import {
  Activity,
  AlertCircle,
  ArrowLeft,
  Award,
  Calendar,
  MapPin,
  Play,
  RefreshCw,
  Settings,
  Users,
} from 'lucide-react';
import StatusBadge from '../components/common/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { matchApi } from '../services/apiClient';
import { canManageMatches, formatMatchDateTime } from '../utils/matchHelpers';

function InningsScorecardView({ inn }) {
  if (!inn) return null;

  const batters = Array.isArray(inn.batters) ? inn.batters : [];
  const activeBatters = batters.filter((b) => !b.didNotBat);
  const didNotBat = batters.filter((b) => b.didNotBat);
  const bowlers = Array.isArray(inn.bowlers) ? inn.bowlers : [];
  const fows = Array.isArray(inn.fallOfWickets) ? inn.fallOfWickets : [];
  const extras = inn.extras || { total: 0, wides: 0, noBalls: 0, byes: 0, legByes: 0 };

  return (
    <div className="space-y-5">
      {/* Innings Total Summary */}
      <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-border bg-canvas p-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-ink-secondary">
            Innings {inn.inningsNumber} • {inn.battingTeamName}
          </span>
          <div className="mt-1 flex items-baseline gap-2.5">
            <span className="text-2xl font-black text-ink tabular-nums">
              {inn.totalRuns}/{inn.wickets}
            </span>
            <span className="text-sm font-semibold text-ink-secondary tabular-nums">
              ({inn.overs} / {inn.maxOvers} Ov)
            </span>
          </div>
        </div>
        <div className="flex items-center gap-4 text-xs">
          <div>
            <span className="text-ink-muted block text-[10px] font-bold uppercase">Run Rate</span>
            <span className="font-extrabold text-ink tabular-nums">
              {inn.runRate != null ? inn.runRate.toFixed(2) : '0.00'}
            </span>
          </div>
          {inn.targetRuns != null && inn.inningsNumber === 2 && (
            <div>
              <span className="text-ink-muted block text-[10px] font-bold uppercase">Target</span>
              <span className="font-extrabold text-brand-hover tabular-nums">{inn.targetRuns}</span>
            </div>
          )}
        </div>
      </div>

      {/* Batting Table */}
      <div className="rounded-2xl border border-border bg-surface overflow-hidden shadow-2xs">
        <div className="bg-canvas px-4 py-2.5 border-b border-border text-xs font-extrabold uppercase tracking-wider text-ink-secondary">
          Batting
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-border text-[11px] text-ink-muted uppercase">
                <th className="py-2.5 px-4">Batter</th>
                <th className="py-2.5 px-4">Dismissal</th>
                <th className="py-2.5 px-3 text-right">R</th>
                <th className="py-2.5 px-3 text-right">B</th>
                <th className="py-2.5 px-3 text-right">4s</th>
                <th className="py-2.5 px-3 text-right">6s</th>
                <th className="py-2.5 px-4 text-right">SR</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {activeBatters.map((b) => (
                <tr key={b.playerId} className="hover:bg-canvas/40">
                  <td className="py-2.5 px-4 font-bold text-ink">
                    {b.playerName}
                    {b.notOut && <span className="ml-1 text-brand">*</span>}
                  </td>
                  <td className="py-2.5 px-4 text-ink-secondary">
                    {b.notOut ? (
                      <span className="font-semibold text-pitch">not out</span>
                    ) : (
                      b.dismissalText || b.dismissalType || 'out'
                    )}
                  </td>
                  <td className="py-2.5 px-3 text-right font-extrabold text-ink tabular-nums">
                    {b.runs}
                  </td>
                  <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                    {b.ballsFaced}
                  </td>
                  <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                    {b.fours}
                  </td>
                  <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                    {b.sixes}
                  </td>
                  <td className="py-2.5 px-4 text-right text-ink-secondary tabular-nums">
                    {b.strikeRate != null ? b.strikeRate.toFixed(1) : '0.0'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Extras & Did Not Bat */}
        <div className="border-t border-border bg-canvas/50 px-4 py-3 text-xs space-y-1.5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="font-semibold text-ink-secondary">Extras</span>
            <span className="font-bold text-ink tabular-nums">
              {extras.total} (w {extras.wides}, nb {extras.noBalls}, b {extras.byes}, lb {extras.legByes})
            </span>
          </div>
          {didNotBat.length > 0 && (
            <div className="text-[11px] text-ink-secondary pt-1">
              <span className="font-bold text-ink">Did not bat: </span>
              {didNotBat.map((p) => p.playerName).join(', ')}
            </div>
          )}
        </div>
      </div>

      {/* Fall of Wickets */}
      {fows.length > 0 && (
        <div className="rounded-2xl border border-border bg-surface p-4 text-xs">
          <h4 className="font-extrabold uppercase tracking-wider text-ink-secondary mb-2 text-[11px]">
            Fall of Wickets
          </h4>
          <div className="flex flex-wrap gap-2">
            {fows.map((f) => (
              <span
                key={f.wicketNumber}
                className="rounded-lg border border-border bg-canvas px-2.5 py-1 text-[11px] font-medium text-ink"
              >
                <strong>
                  {f.score}-{f.wicketNumber}
                </strong>{' '}
                ({f.dismissedPlayerName}, {f.overs} ov)
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Bowling Table */}
      <div className="rounded-2xl border border-border bg-surface overflow-hidden shadow-2xs">
        <div className="bg-canvas px-4 py-2.5 border-b border-border text-xs font-extrabold uppercase tracking-wider text-ink-secondary">
          Bowling ({inn.bowlingTeamName})
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-border text-[11px] text-ink-muted uppercase">
                <th className="py-2.5 px-4">Bowler</th>
                <th className="py-2.5 px-3 text-right">O</th>
                <th className="py-2.5 px-3 text-right">M</th>
                <th className="py-2.5 px-3 text-right">R</th>
                <th className="py-2.5 px-3 text-right">W</th>
                <th className="py-2.5 px-4 text-right">Econ</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {bowlers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-4 text-center text-ink-muted">
                    No overs bowled yet.
                  </td>
                </tr>
              ) : (
                bowlers.map((bw) => (
                  <tr key={bw.playerId} className="hover:bg-canvas/40">
                    <td className="py-2.5 px-4 font-bold text-ink">{bw.playerName}</td>
                    <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                      {bw.overs}
                    </td>
                    <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                      {bw.maidens}
                    </td>
                    <td className="py-2.5 px-3 text-right text-ink-secondary tabular-nums">
                      {bw.runsConceded}
                    </td>
                    <td className="py-2.5 px-3 text-right font-extrabold text-brand-hover tabular-nums">
                      {bw.wickets}
                    </td>
                    <td className="py-2.5 px-4 text-right text-ink-secondary tabular-nums">
                      {bw.economyRate != null ? bw.economyRate.toFixed(2) : '0.00'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export default function MatchDetailPage() {
  const { matchId } = useParams();
  const location = useLocation();
  const { isAuthenticated, roleAssignment } = useAuth();
  const canManage = isAuthenticated && canManageMatches(roleAssignment);

  const isScorecardRoute = location.pathname.endsWith('/scorecard');
  const [activeTab, setActiveTab] = useState(isScorecardRoute ? 'scorecard' : 'scorecard');
  const [selectedInningsNum, setSelectedInningsNum] = useState(null);

  const [scorecard, setScorecard] = useState(null);
  const [match, setMatch] = useState(null);
  const [toss, setToss] = useState(null);
  const [playingXI, setPlayingXI] = useState(null);
  const [deliveries, setDeliveries] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = useCallback(async () => {
    setError('');
    try {
      // Public Scorecard endpoint works without login
      const scRes = await matchApi.getScorecard(matchId);
      setScorecard(scRes);

      if (selectedInningsNum === null) {
        if (scRes?.innings2) setSelectedInningsNum(2);
        else if (scRes?.innings1) setSelectedInningsNum(1);
      }

      if (isAuthenticated) {
        const [mRes, tossRes, xiRes, delRes] = await Promise.allSettled([
          matchApi.getById(matchId),
          matchApi.getToss(matchId),
          matchApi.getPlayingXI(matchId),
          matchApi.getDeliveries(matchId),
        ]);
        if (mRes.status === 'fulfilled') setMatch(mRes.value);
        if (tossRes.status === 'fulfilled') setToss(tossRes.value);
        if (xiRes.status === 'fulfilled') setPlayingXI(xiRes.value);
        if (delRes.status === 'fulfilled') setDeliveries(delRes.value || []);
      }
    } catch (err) {
      setError(err.message || 'Unable to load match scorecard.');
    } finally {
      setLoading(false);
    }
  }, [matchId, isAuthenticated, selectedInningsNum]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Auto-refresh every 8 seconds while match is IN_PROGRESS
  useEffect(() => {
    if (scorecard?.matchStatus !== 'IN_PROGRESS') return;
    const timer = setInterval(() => {
      loadData();
    }, 8000);
    return () => clearInterval(timer);
  }, [scorecard?.matchStatus, loadData]);

  const status = match?.status || scorecard?.matchStatus;
  const inn1 = scorecard?.innings1;
  const inn2 = scorecard?.innings2;
  const displayInnings = selectedInningsNum === 2 && inn2 ? inn2 : inn1;

  const teamAName = match?.teamAName || inn1?.battingTeamName || 'Team A';
  const teamBName = match?.teamBName || inn1?.bowlingTeamName || 'Team B';

  return (
    <div className="space-y-5 max-w-4xl mx-auto">
      <div className="flex items-center justify-between gap-3">
        <Link
          to="/matches"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-ink-secondary hover:text-ink"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Matches
        </Link>

        <button
          type="button"
          onClick={loadData}
          className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted cursor-pointer"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          Refresh
        </button>
      </div>

      {loading ? (
        <div className="h-64 animate-pulse rounded-2xl border border-border bg-surface p-6" />
      ) : error ? (
        <div className="rounded-2xl border border-live-soft bg-live-soft/50 p-6 text-sm text-live flex items-center gap-2">
          <AlertCircle className="h-5 w-5 shrink-0" />
          <span>{error}</span>
        </div>
      ) : (
        <>
          {/* Match Hero Header */}
          <section className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)] space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge status={status} />
                {match?.tournamentName && (
                  <span className="text-xs font-bold text-brand-hover">{match.tournamentName}</span>
                )}
              </div>

              {canManage && (
                <div className="flex items-center gap-2">
                  {status === 'SCHEDULED' && (
                    <Link
                      to={`/matches/${matchId}/setup`}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-3.5 py-2 text-xs font-bold text-white hover:bg-brand-hover"
                    >
                      <Settings className="h-3.5 w-3.5" />
                      Setup Match (XI & Toss)
                    </Link>
                  )}
                  {status === 'IN_PROGRESS' && (
                    <Link
                      to={`/matches/${matchId}/score`}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-live px-3.5 py-2 text-xs font-bold text-white hover:opacity-90"
                    >
                      <Play className="h-3.5 w-3.5" />
                      Open Live Scorer
                    </Link>
                  )}
                </div>
              )}
            </div>

            <div>
              <h1 className="text-2xl font-extrabold tracking-tight text-ink">
                {teamAName} vs {teamBName}
              </h1>
              {match && (
                <div className="mt-1.5 flex flex-wrap items-center gap-4 text-xs text-ink-secondary">
                  <span className="inline-flex items-center gap-1">
                    <Calendar className="h-3.5 w-3.5 text-ink-muted" />
                    {formatMatchDateTime(match.matchDateTime)}
                  </span>
                  {match.venue && (
                    <span className="inline-flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5 text-ink-muted" />
                      {match.venue}
                    </span>
                  )}
                  <span>
                    {match.format} ({match.maxOvers} Ov)
                  </span>
                </div>
              )}
            </div>

            {/* Score Strips */}
            {(inn1 || inn2) && (
              <div className="grid gap-3 sm:grid-cols-2 pt-3 border-t border-border/60">
                {inn1 && (
                  <div className="rounded-xl border border-border bg-canvas p-3.5 flex items-center justify-between">
                    <span className="font-bold text-sm text-ink">{inn1.battingTeamName}</span>
                    <span className="text-base font-black text-ink tabular-nums">
                      {inn1.totalRuns}/{inn1.wickets}{' '}
                      <span className="text-xs font-semibold text-ink-secondary">({inn1.overs} ov)</span>
                    </span>
                  </div>
                )}
                {inn2 && (
                  <div className="rounded-xl border border-border bg-canvas p-3.5 flex items-center justify-between">
                    <span className="font-bold text-sm text-ink">{inn2.battingTeamName}</span>
                    <span className="text-base font-black text-ink tabular-nums">
                      {inn2.totalRuns}/{inn2.wickets}{' '}
                      <span className="text-xs font-semibold text-ink-secondary">({inn2.overs} ov)</span>
                    </span>
                  </div>
                )}
              </div>
            )}

            {/* Toss / Result Banner */}
            {scorecard?.result?.resultSummary ? (
              <div className="rounded-xl border border-pitch/30 bg-pitch-soft/60 px-4 py-2.5 text-xs font-bold text-pitch flex items-center gap-2">
                <Award className="h-4 w-4 shrink-0" />
                <span>{scorecard.result.resultSummary}</span>
              </div>
            ) : toss?.tossWinnerTeamName ? (
              <div className="rounded-xl border border-border bg-canvas px-4 py-2 text-xs font-medium text-ink-secondary">
                Toss: <strong className="text-ink">{toss.tossWinnerTeamName}</strong> won the toss and elected to{' '}
                <strong className="text-ink">{toss.decision?.toLowerCase()}</strong>.
              </div>
            ) : null}
          </section>

          {/* Navigation Tabs */}
          <div className="flex flex-wrap items-center gap-2 border-b border-border pb-2">
            {[
              { id: 'scorecard', label: 'Scorecard', icon: Activity },
              ...(playingXI ? [{ id: 'xi', label: 'Playing XI', icon: Users }] : []),
              ...(deliveries.length > 0 ? [{ id: 'commentary', label: 'Ball-by-Ball', icon: Play }] : []),
            ].map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  type="button"
                  onClick={() => setActiveTab(tab.id)}
                  className={`inline-flex items-center gap-1.5 rounded-xl px-4 py-2 text-xs font-bold transition-colors cursor-pointer ${
                    activeTab === tab.id
                      ? 'bg-brand text-white'
                      : 'bg-surface border border-border text-ink-secondary hover:text-ink'
                  }`}
                >
                  <Icon className="h-3.5 w-3.5" />
                  {tab.label}
                </button>
              );
            })}
          </div>

          {/* Tab Content: Scorecard */}
          {activeTab === 'scorecard' && (
            <div className="space-y-4">
              {!inn1 && !inn2 ? (
                <div className="rounded-2xl border border-dashed border-border bg-surface p-8 text-center text-sm text-ink-secondary">
                  Match has not started yet. Scorecard will appear once Innings 1 begins.
                </div>
              ) : (
                <>
                  {inn1 && inn2 && (
                    <div className="flex gap-2">
                      {[inn1, inn2].map((i) => (
                        <button
                          key={i.inningsNumber}
                          type="button"
                          onClick={() => setSelectedInningsNum(i.inningsNumber)}
                          className={`rounded-xl px-4 py-2 text-xs font-bold border transition-colors cursor-pointer ${
                            (displayInnings?.inningsNumber ?? 1) === i.inningsNumber
                              ? 'border-brand bg-brand-soft text-brand-hover'
                              : 'border-border bg-surface text-ink-secondary hover:text-ink'
                          }`}
                        >
                          {i.battingTeamName} (Innings {i.inningsNumber})
                        </button>
                      ))}
                    </div>
                  )}
                  <InningsScorecardView inn={displayInnings} />
                </>
              )}
            </div>
          )}

          {/* Tab Content: Playing XI */}
          {activeTab === 'xi' && playingXI && (
            <div className="grid gap-5 md:grid-cols-2">
              {[
                { name: playingXI.teamAName || teamAName, list: playingXI.teamAPlayers || [] },
                { name: playingXI.teamBName || teamBName, list: playingXI.teamBPlayers || [] },
              ].map((tm, idx) => (
                <div key={idx} className="rounded-2xl border border-border bg-surface p-5 space-y-3">
                  <h3 className="text-base font-extrabold text-ink border-b border-border pb-2">
                    {tm.name} Playing XI ({tm.list.length})
                  </h3>
                  {tm.list.length === 0 ? (
                    <p className="text-xs text-ink-muted">Playing XI not announced yet.</p>
                  ) : (
                    <div className="space-y-2">
                      {tm.list.map((p) => (
                        <div
                          key={p.playerId}
                          className="flex items-center justify-between rounded-xl border border-border bg-canvas px-3 py-2 text-xs"
                        >
                          <div>
                            <span className="font-bold text-ink">{p.playerName}</span>
                            {p.captain && (
                              <span className="ml-1.5 rounded bg-brand px-1.5 py-0.2 text-[10px] font-bold text-white">
                                C
                              </span>
                            )}
                            {p.wicketkeeper && (
                              <span className="ml-1 rounded bg-pitch px-1.5 py-0.2 text-[10px] font-bold text-white">
                                WK
                              </span>
                            )}
                          </div>
                          <span className="text-[11px] text-ink-secondary">
                            {p.playingRole?.replace('_', ' ')}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}

          {/* Tab Content: Ball-by-Ball Commentary */}
          {activeTab === 'commentary' && (
            <div className="rounded-2xl border border-border bg-surface p-5 space-y-2.5">
              <h3 className="text-base font-extrabold text-ink border-b border-border pb-2">
                Ball-by-Ball Log ({deliveries.length} deliveries)
              </h3>
              <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
                {[...deliveries].reverse().map((d) => (
                  <div
                    key={d.id}
                    className="flex items-center justify-between gap-3 rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-xs"
                  >
                    <div className="flex items-center gap-3">
                      <span className="font-mono font-bold text-ink-secondary w-9">{d.overText}</span>
                      <div>
                        <span className="font-bold text-ink">
                          {d.bowlerName} to {d.strikerName}
                        </span>
                        {d.isWicket && (
                          <span className="ml-2 font-bold text-live">
                            OUT! ({d.dismissalType?.replace('_', ' ')})
                          </span>
                        )}
                        {d.extraType && d.extraType !== 'NONE' && (
                          <span className="ml-2 text-amber-800 font-semibold">
                            [{d.extraType.replace('_', ' ')}]
                          </span>
                        )}
                      </div>
                    </div>
                    <span
                      className={`rounded-full px-2.5 py-0.5 font-extrabold tabular-nums ${
                        d.isWicket
                          ? 'bg-live text-white'
                          : d.totalRuns >= 4
                            ? 'bg-brand text-white'
                            : 'bg-surface border border-border text-ink'
                      }`}
                    >
                      {d.isWicket ? 'W' : `${d.totalRuns} run${d.totalRuns === 1 ? '' : 's'}`}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
