import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  Activity,
  AlertCircle,
  ArrowLeft,
  Award,
  CheckCircle2,
  ClipboardList,
  Loader2,
  Play,
  RefreshCw,
  ShieldAlert,
  UserCheck,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import StatusBadge from '../components/common/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { matchApi } from '../services/apiClient';
import { canManageMatches } from '../utils/matchHelpers';

const DISMISSAL_OPTIONS = [
  { value: 'BOWLED', label: 'Bowled' },
  { value: 'CAUGHT', label: 'Caught' },
  { value: 'LBW', label: 'LBW' },
  { value: 'RUN_OUT', label: 'Run Out' },
  { value: 'STUMPED', label: 'Stumped' },
  { value: 'HIT_WICKET', label: 'Hit Wicket' },
  { value: 'OBSTRUCTING_FIELD', label: 'Obstructing the Field' },
];

function formatBallChip(d) {
  if (d.isWicket) return { label: 'W', cls: 'bg-live text-white' };
  if (d.extraType === 'WIDE') {
    return {
      label: d.extraRuns > 1 ? `Wd+${d.extraRuns - 1}` : 'Wd',
      cls: 'bg-amber-100 text-amber-900 border border-amber-300',
    };
  }
  if (d.extraType === 'NO_BALL') {
    return {
      label: d.batterRuns > 0 ? `Nb+${d.batterRuns}` : 'Nb',
      cls: 'bg-amber-100 text-amber-900 border border-amber-300',
    };
  }
  if (d.extraType === 'BYE') {
    return { label: `B${d.extraRuns}`, cls: 'bg-surface-muted text-ink border border-border' };
  }
  if (d.extraType === 'LEG_BYE') {
    return { label: `Lb${d.extraRuns}`, cls: 'bg-surface-muted text-ink border border-border' };
  }
  if (d.batterRuns === 4) return { label: '4', cls: 'bg-sky-600 text-white' };
  if (d.batterRuns === 6) return { label: '6', cls: 'bg-brand text-white' };
  if (d.batterRuns === 0) return { label: '•', cls: 'bg-canvas text-ink-secondary border border-border' };
  return { label: String(d.batterRuns), cls: 'bg-surface text-ink border border-border font-bold' };
}

export default function LiveScoringPage() {
  const { matchId } = useParams();
  const { isAuthenticated, roleAssignment } = useAuth();
  const canScore = isAuthenticated && canManageMatches(roleAssignment);

  const [match, setMatch] = useState(null);
  const [inningsList, setInningsList] = useState([]);
  const [playingXI, setPlayingXI] = useState({ teamAPlayers: [], teamBPlayers: [] });
  const [scorecard, setScorecard] = useState(null);
  const [deliveries, setDeliveries] = useState([]);

  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  // Next bowler modal/state when over finishes
  const [nextBowlerId, setNextBowlerId] = useState('');

  // Extras quick drawer state: null | 'WIDE' | 'NO_BALL' | 'BYE' | 'LEG_BYE'
  const [activeExtraMode, setActiveExtraMode] = useState(null);

  // Wicket modal state
  const [wicketModalOpen, setWicketModalOpen] = useState(false);
  const [dismissalType, setDismissalType] = useState('BOWLED');
  const [dismissedPlayerId, setDismissedPlayerId] = useState('');
  const [fielderId, setFielderId] = useState('');
  const [nextBatterPlayerId, setNextBatterPlayerId] = useState('');
  const [wicketRunOutRuns, setWicketRunOutRuns] = useState(0);

  // Innings 2 Start state
  const [inn2StrikerId, setInn2StrikerId] = useState('');
  const [inn2NonStrikerId, setInn2NonStrikerId] = useState('');
  const [inn2BowlerId, setInn2BowlerId] = useState('');

  const loadScorerState = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    setError('');
    try {
      const [mData, innRes, xiRes, scRes, delRes] = await Promise.all([
        matchApi.getById(matchId),
        matchApi.getInnings(matchId).catch(() => []),
        matchApi.getPlayingXI(matchId).catch(() => ({ teamAPlayers: [], teamBPlayers: [] })),
        matchApi.getScorecard(matchId).catch(() => null),
        matchApi.getDeliveries(matchId).catch(() => []),
      ]);
      setMatch(mData);
      setInningsList(Array.isArray(innRes) ? innRes : []);
      setPlayingXI(xiRes || { teamAPlayers: [], teamBPlayers: [] });
      setScorecard(scRes);
      setDeliveries(Array.isArray(delRes) ? delRes : []);
    } catch (err) {
      setError(err.message || 'Failed to load live scoring state.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, matchId]);

  useEffect(() => {
    loadScorerState();
  }, [loadScorerState]);

  const activeInnings = useMemo(
    () => inningsList.find((i) => i.status === 'IN_PROGRESS') || null,
    [inningsList]
  );

  const innings1 = useMemo(
    () => inningsList.find((i) => i.inningsNumber === 1) || null,
    [inningsList]
  );

  const innings2 = useMemo(
    () => inningsList.find((i) => i.inningsNumber === 2) || null,
    [inningsList]
  );

  const awaitingInnings2 =
    match?.status === 'IN_PROGRESS' &&
    innings1?.status === 'COMPLETED' &&
    !innings2;

  // Determine batting & bowling Playing XIs for active innings (or upcoming innings 2)
  const { battingXI, bowlingXI } = useMemo(() => {
    if (!match) return { battingXI: [], bowlingXI: [] };
    const targetInn = activeInnings || (awaitingInnings2 ? innings1 : null);
    if (!targetInn) return { battingXI: [], bowlingXI: [] };

    const teamA = playingXI?.teamAPlayers || [];
    const teamB = playingXI?.teamBPlayers || [];

    if (awaitingInnings2) {
      // Flip teams for Innings 2
      const inn1BatIsA = String(innings1.battingTeamId) === String(match.teamAId);
      return {
        battingXI: inn1BatIsA ? teamB : teamA,
        bowlingXI: inn1BatIsA ? teamA : teamB,
      };
    }

    const batIsA = String(activeInnings.battingTeamId) === String(match.teamAId);
    return {
      battingXI: batIsA ? teamA : teamB,
      bowlingXI: batIsA ? teamB : teamA,
    };
  }, [match, activeInnings, awaitingInnings2, innings1, playingXI]);

  // Pre-fill Innings 2 opening pair & bowler when Innings 1 completes
  useEffect(() => {
    if (!awaitingInnings2) return;
    if (battingXI.length >= 2) {
      if (!inn2StrikerId) setInn2StrikerId(String(battingXI[0].playerId));
      if (!inn2NonStrikerId) setInn2NonStrikerId(String(battingXI[1].playerId));
    }
    if (bowlingXI.length >= 1 && !inn2BowlerId) {
      const pref =
        bowlingXI.find((p) => p.playingRole === 'BOWLER' || p.playingRole === 'ALL_ROUNDER') ||
        bowlingXI[bowlingXI.length - 1];
      setInn2BowlerId(String(pref.playerId));
    }
  }, [awaitingInnings2, battingXI, bowlingXI, inn2StrikerId, inn2NonStrikerId, inn2BowlerId]);

  const activeScorecardInnings = useMemo(() => {
    if (!scorecard || !activeInnings) return null;
    return activeInnings.inningsNumber === 2 ? scorecard.innings2 : scorecard.innings1;
  }, [scorecard, activeInnings]);

  const strikerStats = useMemo(() => {
    if (!activeScorecardInnings?.batters || !activeInnings?.currentStrikerId) return null;
    return activeScorecardInnings.batters.find(
      (b) => String(b.playerId) === String(activeInnings.currentStrikerId)
    );
  }, [activeScorecardInnings, activeInnings]);

  const nonStrikerStats = useMemo(() => {
    if (!activeScorecardInnings?.batters || !activeInnings?.currentNonStrikerId) return null;
    return activeScorecardInnings.batters.find(
      (b) => String(b.playerId) === String(activeInnings.currentNonStrikerId)
    );
  }, [activeScorecardInnings, activeInnings]);

  const bowlerStats = useMemo(() => {
    if (!activeScorecardInnings?.bowlers || !activeInnings?.currentBowlerId) return null;
    return activeScorecardInnings.bowlers.find(
      (b) => String(b.playerId) === String(activeInnings.currentBowlerId)
    );
  }, [activeScorecardInnings, activeInnings]);

  const activeInningsDeliveries = useMemo(() => {
    if (!activeInnings) return [];
    return deliveries.filter((d) => String(d.inningsId) === String(activeInnings.id));
  }, [deliveries, activeInnings]);

  const previousOverBowlerId = useMemo(() => {
    if (activeInningsDeliveries.length === 0) return null;
    return activeInningsDeliveries[activeInningsDeliveries.length - 1].bowlerId;
  }, [activeInningsDeliveries]);

  // Available next batters (not dismissed, not currently at crease)
  const availableNextBatters = useMemo(() => {
    if (!activeInnings) return [];
    const dismissedIds = new Set(
      (activeScorecardInnings?.batters || [])
        .filter((b) => !b.notOut && !b.didNotBat)
        .map((b) => String(b.playerId))
    );
    return battingXI.filter(
      (p) =>
        String(p.playerId) !== String(activeInnings.currentStrikerId) &&
        String(p.playerId) !== String(activeInnings.currentNonStrikerId) &&
        !dismissedIds.has(String(p.playerId))
    );
  }, [activeInnings, activeScorecardInnings, battingXI]);

  const recordBall = async (payload) => {
    setSubmitting(true);
    setError('');
    setFeedback('');
    setActiveExtraMode(null);
    try {
      await matchApi.recordDelivery(matchId, payload);
      await loadScorerState();
    } catch (err) {
      setError(err.message || 'Failed to record delivery.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleChangeBowler = async (e) => {
    e.preventDefault();
    if (!nextBowlerId) {
      setError('Please select a bowler for the next over.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await matchApi.changeBowler(matchId, { bowlerPlayerId: Number(nextBowlerId) });
      setNextBowlerId('');
      setFeedback('Bowler updated for the new over.');
      await loadScorerState();
    } catch (err) {
      setError(err.message || 'Failed to change bowler.');
    } finally {
      setSubmitting(false);
    }
  };

  const openWicketModal = () => {
    if (!activeInnings) return;
    setDismissalType('BOWLED');
    setDismissedPlayerId(String(activeInnings.currentStrikerId || ''));
    setFielderId('');
    setWicketRunOutRuns(0);
    setNextBatterPlayerId(
      availableNextBatters.length > 0 ? String(availableNextBatters[0].playerId) : ''
    );
    setWicketModalOpen(true);
  };

  const handleConfirmWicket = async (e) => {
    e.preventDefault();
    const isLastWicket = (activeInnings?.wickets ?? 0) >= 9;
    if (!isLastWicket && !nextBatterPlayerId) {
      setError('Please select the incoming next batter.');
      return;
    }

    const needsFielder = ['CAUGHT', 'RUN_OUT', 'STUMPED'].includes(dismissalType);
    const payload = {
      batterRuns: dismissalType === 'RUN_OUT' ? Number(wicketRunOutRuns) : 0,
      extraType: 'NONE',
      extraRuns: 0,
      isWicket: true,
      dismissalType,
      dismissedPlayerId: Number(dismissedPlayerId),
      fielderId: needsFielder && fielderId ? Number(fielderId) : null,
      nextBatterPlayerId: isLastWicket ? null : Number(nextBatterPlayerId),
    };

    setWicketModalOpen(false);
    await recordBall(payload);
  };

  const handleStartInnings2 = async (e) => {
    e.preventDefault();
    if (!inn2StrikerId || !inn2NonStrikerId || !inn2BowlerId) {
      setError('Select Opening Striker, Non-Striker, and Opening Bowler for 2nd Innings.');
      return;
    }
    if (inn2StrikerId === inn2NonStrikerId) {
      setError('Striker and Non-Striker must be different.');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      await matchApi.startInnings(matchId, {
        inningsNumber: 2,
        strikerPlayerId: Number(inn2StrikerId),
        nonStrikerPlayerId: Number(inn2NonStrikerId),
        bowlerPlayerId: Number(inn2BowlerId),
      });
      setFeedback('2nd Innings started!');
      await loadScorerState();
    } catch (err) {
      setError(err.message || 'Failed to start 2nd innings.');
    } finally {
      setSubmitting(false);
    }
  };

  const needsBowlerSelection =
    activeInnings &&
    activeInnings.status === 'IN_PROGRESS' &&
    !activeInnings.currentBowlerId;

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
        <div className="flex items-center gap-2">
          <Link
            to={`/matches/${matchId}/scorecard`}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-semibold text-brand-hover hover:bg-surface-muted"
          >
            <ClipboardList className="h-3.5 w-3.5" />
            Full Scorecard
          </Link>
          <button
            type="button"
            onClick={loadScorerState}
            disabled={loading}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted cursor-pointer"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {!isAuthenticated ? (
        <AuthRequiredCard
          title="Sign in to open Live Scorer"
          description="Only authorized match officials can record deliveries."
        />
      ) : !canScore ? (
        <div className="rounded-2xl border border-live-soft bg-live-soft/50 p-6 text-sm text-live flex items-center gap-2">
          <ShieldAlert className="h-5 w-5 shrink-0" />
          <span>Only Platform Administrators and College Administrators can operate the live scoring console.</span>
        </div>
      ) : loading ? (
        <div className="h-64 animate-pulse rounded-2xl border border-border bg-surface p-6" />
      ) : !match ? (
        <div className="rounded-2xl border border-live-soft bg-live-soft/50 p-6 text-sm text-live">
          {error || 'Match not found.'}
        </div>
      ) : match.status === 'SCHEDULED' ? (
        <div className="rounded-2xl border border-border bg-surface p-8 text-center space-y-4">
          <h2 className="text-xl font-extrabold text-ink">Match Setup Required</h2>
          <p className="text-sm text-ink-secondary">
            Complete Playing XI selection, Toss recording, and Innings 1 initialization before scoring.
          </p>
          <Link
            to={`/matches/${matchId}/setup`}
            className="inline-flex items-center gap-2 rounded-xl bg-brand px-5 py-2.5 text-sm font-bold text-white hover:bg-brand-hover"
          >
            Go to Match Setup →
          </Link>
        </div>
      ) : (
        <>
          {feedback && (
            <div className="flex items-start gap-2 rounded-xl border border-pitch-soft bg-pitch-soft/70 p-3 text-xs font-semibold text-pitch">
              <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{feedback}</span>
            </div>
          )}

          {error && (
            <div className="flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/70 p-3 text-xs font-semibold text-live">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Match Complete Result Banner */}
          {match.status === 'COMPLETED' && (
            <section className="rounded-2xl border border-pitch/30 bg-pitch-soft/60 p-6 text-center space-y-3">
              <div className="mx-auto inline-flex h-12 w-12 items-center justify-center rounded-full bg-pitch text-white">
                <Award className="h-6 w-6" />
              </div>
              <h2 className="text-2xl font-black text-ink">Match Completed!</h2>
              <p className="text-base font-bold text-pitch">
                {scorecard?.result?.resultSummary || match.resultSummary || 'Result Recorded'}
              </p>
              <div className="pt-2">
                <Link
                  to={`/matches/${matchId}/scorecard`}
                  className="inline-flex items-center gap-2 rounded-xl bg-brand px-5 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-brand-hover"
                >
                  View Full Public Scorecard →
                </Link>
              </div>
            </section>
          )}

          {/* Live Scoreboard Summary Banner */}
          {(activeInnings || innings1) && (
            <section className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)]">
              <div className="flex flex-wrap items-center justify-between gap-2 pb-4 border-b border-border/60">
                <div className="flex items-center gap-2">
                  <StatusBadge status={match.status} />
                  <span className="text-xs font-bold text-ink-secondary">
                    {match.tournamentName} • {match.venue}
                  </span>
                </div>
                <span className="text-xs font-bold text-brand-hover">
                  {activeInnings
                    ? `Innings ${activeInnings.inningsNumber} (${activeInnings.maxOvers} Ov)`
                    : `Innings 1 Complete`}
                </span>
              </div>

              {activeInnings && (
                <div className="mt-4 flex flex-col sm:flex-row sm:items-end justify-between gap-4">
                  <div>
                    <p className="text-xs font-bold uppercase tracking-wider text-ink-secondary">
                      {activeInnings.battingTeamName} Batting
                    </p>
                    <div className="mt-1 flex items-baseline gap-3">
                      <span className="text-4xl font-black tracking-tight text-ink tabular-nums">
                        {activeInnings.totalRuns}/{activeInnings.wickets}
                      </span>
                      <span className="text-lg font-bold text-ink-secondary tabular-nums">
                        ({activeInnings.overs} / {activeInnings.maxOvers} ov)
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-3 text-xs">
                    <div className="rounded-xl border border-border bg-canvas px-3.5 py-2">
                      <span className="text-ink-muted block text-[10px] font-bold uppercase">CRR</span>
                      <span className="font-extrabold text-ink tabular-nums">
                        {activeScorecardInnings?.runRate != null
                          ? activeScorecardInnings.runRate.toFixed(2)
                          : '0.00'}
                      </span>
                    </div>
                    {activeInnings.inningsNumber === 2 && activeInnings.targetRuns != null && (
                      <div className="rounded-xl border border-brand/30 bg-brand-soft/40 px-3.5 py-2">
                        <span className="text-brand-hover block text-[10px] font-bold uppercase">
                          Target: {activeInnings.targetRuns}
                        </span>
                        <span className="font-extrabold text-ink tabular-nums">
                          Need {Math.max(0, activeInnings.targetRuns - activeInnings.totalRuns)} in{' '}
                          {Math.max(0, activeInnings.maxOvers * 6 - activeInnings.legalBalls)} balls
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Crease & Bowler Strip */}
              {activeInnings && activeInnings.status === 'IN_PROGRESS' && (
                <div className="mt-5 grid gap-3 sm:grid-cols-3 pt-4 border-t border-border/60 text-xs">
                  <div className="rounded-xl border border-brand/30 bg-brand-soft/20 p-3">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-brand-hover">
                      Striker *
                    </span>
                    <p className="mt-0.5 text-sm font-extrabold text-ink truncate">
                      {activeInnings.currentStrikerName || '—'}
                    </p>
                    <p className="text-ink-secondary tabular-nums mt-0.5">
                      {strikerStats ? `${strikerStats.runs} (${strikerStats.ballsFaced}) • SR ${strikerStats.strikeRate?.toFixed(1)}` : '0 (0)'}
                    </p>
                  </div>

                  <div className="rounded-xl border border-border bg-canvas p-3">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-ink-secondary">
                      Non-Striker
                    </span>
                    <p className="mt-0.5 text-sm font-extrabold text-ink truncate">
                      {activeInnings.currentNonStrikerName || '—'}
                    </p>
                    <p className="text-ink-secondary tabular-nums mt-0.5">
                      {nonStrikerStats ? `${nonStrikerStats.runs} (${nonStrikerStats.ballsFaced}) • SR ${nonStrikerStats.strikeRate?.toFixed(1)}` : '0 (0)'}
                    </p>
                  </div>

                  <div className="rounded-xl border border-border bg-canvas p-3">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-ink-secondary">
                      Current Bowler
                    </span>
                    <p className="mt-0.5 text-sm font-extrabold text-ink truncate">
                      {activeInnings.currentBowlerName || 'Select Next Bowler'}
                    </p>
                    <p className="text-ink-secondary tabular-nums mt-0.5">
                      {bowlerStats
                        ? `${bowlerStats.overs}-${bowlerStats.maidens}-${bowlerStats.runsConceded}-${bowlerStats.wickets} (Econ ${bowlerStats.economyRate?.toFixed(1)})`
                        : 'Awaiting bowler'}
                    </p>
                  </div>
                </div>
              )}

              {/* Recent Balls Timeline */}
              {activeInningsDeliveries.length > 0 && (
                <div className="mt-4 pt-4 border-t border-border/60 flex items-center gap-2 overflow-x-auto">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-ink-muted shrink-0 mr-1">
                    Recent Balls:
                  </span>
                  {activeInningsDeliveries.slice(-12).map((d) => {
                    const chip = formatBallChip(d);
                    return (
                      <span
                        key={d.id}
                        title={`${d.overText}: ${d.bowlerName} to ${d.strikerName}`}
                        className={`inline-flex h-7 min-w-7 px-2 items-center justify-center rounded-full text-xs font-bold shrink-0 ${chip.cls}`}
                      >
                        {chip.label}
                      </span>
                    );
                  })}
                </div>
              )}
            </section>
          )}

          {/* Start Innings 2 Panel */}
          {awaitingInnings2 && (
            <section className="rounded-2xl border border-brand/30 bg-surface p-6 shadow-sm space-y-4">
              <div className="border-b border-border pb-3">
                <span className="inline-flex items-center gap-1 rounded-full bg-pitch-soft px-2.5 py-0.5 text-[11px] font-bold text-pitch">
                  Innings Break
                </span>
                <h2 className="mt-2 text-lg font-extrabold text-ink">
                  Start 2nd Innings • Target {innings1.totalRuns + 1} Runs
                </h2>
                <p className="text-xs text-ink-secondary">
                  {innings1.battingTeamName} scored {innings1.totalRuns}/{innings1.wickets} in{' '}
                  {innings1.overs} overs. Select openers for {innings1.bowlingTeamName}.
                </p>
              </div>

              <form onSubmit={handleStartInnings2} className="grid gap-4 sm:grid-cols-3 items-end">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Opening Striker *
                  </label>
                  <select
                    required
                    value={inn2StrikerId}
                    onChange={(e) => setInn2StrikerId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value="">-- Select Striker --</option>
                    {battingXI.map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Opening Non-Striker *
                  </label>
                  <select
                    required
                    value={inn2NonStrikerId}
                    onChange={(e) => setInn2NonStrikerId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value="">-- Select Non-Striker --</option>
                    {battingXI.map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Opening Bowler *
                  </label>
                  <select
                    required
                    value={inn2BowlerId}
                    onChange={(e) => setInn2BowlerId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value="">-- Select Bowler --</option>
                    {bowlingXI.map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="sm:col-span-3 flex justify-end">
                  <button
                    type="submit"
                    disabled={submitting}
                    className="inline-flex items-center gap-2 rounded-xl bg-pitch px-6 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-pitch-hover cursor-pointer"
                  >
                    {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Play className="h-4 w-4" />}
                    Start 2nd Innings
                  </button>
                </div>
              </form>
            </section>
          )}

          {/* Over Complete -> Select Next Bowler Prompt */}
          {needsBowlerSelection && (
            <section className="rounded-2xl border border-amber-300 bg-amber-50 p-6 shadow-sm space-y-4">
              <div className="flex items-center gap-2.5">
                <UserCheck className="h-5 w-5 text-amber-700" />
                <div>
                  <h2 className="text-base font-extrabold text-amber-950">
                    Over Complete! Select Bowler for Over #{Math.floor(activeInnings.legalBalls / 6) + 1}
                  </h2>
                  <p className="text-xs text-amber-800">
                    The previous bowler cannot bowl consecutive overs.
                  </p>
                </div>
              </div>

              <form onSubmit={handleChangeBowler} className="flex flex-col sm:flex-row gap-3">
                <select
                  required
                  value={nextBowlerId}
                  onChange={(e) => setNextBowlerId(e.target.value)}
                  className="flex-1 rounded-xl border border-amber-300 bg-white px-3.5 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                >
                  <option value="">-- Select Next Bowler --</option>
                  {bowlingXI
                    .filter((p) => String(p.playerId) !== String(previousOverBowlerId))
                    .map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName} ({p.playingRole?.replace('_', ' ')})
                      </option>
                    ))}
                </select>
                <button
                  type="submit"
                  disabled={submitting || !nextBowlerId}
                  className="rounded-xl bg-brand px-6 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-brand-hover disabled:opacity-50 cursor-pointer"
                >
                  {submitting ? 'Setting Bowler...' : 'Confirm Bowler'}
                </button>
              </form>
            </section>
          )}

          {/* Ball-by-Ball Scoring Keypad */}
          {activeInnings &&
            activeInnings.status === 'IN_PROGRESS' &&
            !needsBowlerSelection && (
              <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm space-y-5">
                <div className="flex items-center justify-between border-b border-border pb-3">
                  <div className="flex items-center gap-2">
                    <Activity className="h-5 w-5 text-brand" />
                    <h2 className="text-base font-extrabold text-ink">Ball-by-Ball Scoring Keypad</h2>
                  </div>
                  {submitting && (
                    <span className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand">
                      <Loader2 className="h-3.5 w-3.5 animate-spin" />
                      Recording...
                    </span>
                  )}
                </div>

                {/* Standard Runs */}
                <div>
                  <span className="block text-[11px] font-bold uppercase tracking-wider text-ink-secondary mb-2">
                    Legal Delivery Runs
                  </span>
                  <div className="grid grid-cols-3 sm:grid-cols-6 gap-2.5">
                    {[
                      { runs: 0, label: '0', sub: 'Dot', style: 'bg-canvas border-border text-ink hover:bg-surface-muted' },
                      { runs: 1, label: '1', sub: 'Single', style: 'bg-canvas border-border text-ink hover:bg-surface-muted' },
                      { runs: 2, label: '2', sub: 'Double', style: 'bg-canvas border-border text-ink hover:bg-surface-muted' },
                      { runs: 3, label: '3', sub: 'Three', style: 'bg-canvas border-border text-ink hover:bg-surface-muted' },
                      { runs: 4, label: '4', sub: 'FOUR', style: 'bg-sky-50 border-sky-200 text-sky-800 hover:bg-sky-100' },
                      { runs: 6, label: '6', sub: 'SIX', style: 'bg-brand-soft border-brand/30 text-brand-hover hover:bg-brand-soft/80' },
                    ].map((btn) => (
                      <button
                        key={btn.runs}
                        type="button"
                        disabled={submitting}
                        onClick={() =>
                          recordBall({
                            batterRuns: btn.runs,
                            extraType: 'NONE',
                            extraRuns: 0,
                            isWicket: false,
                          })
                        }
                        className={`rounded-2xl border py-3.5 text-center transition-all cursor-pointer disabled:opacity-50 shadow-2xs ${btn.style}`}
                      >
                        <div className="text-2xl font-black leading-none">{btn.label}</div>
                        <div className="text-[10px] font-bold uppercase tracking-wider mt-1 opacity-80">
                          {btn.sub}
                        </div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Extras & Wicket Row */}
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5 pt-2 border-t border-border/60">
                  {[
                    { key: 'WIDE', label: 'Wide (Wd)' },
                    { key: 'NO_BALL', label: 'No Ball (Nb)' },
                    { key: 'BYE', label: 'Bye (B)' },
                    { key: 'LEG_BYE', label: 'Leg Bye (Lb)' },
                  ].map((ex) => (
                    <button
                      key={ex.key}
                      type="button"
                      disabled={submitting}
                      onClick={() =>
                        setActiveExtraMode((prev) => (prev === ex.key ? null : ex.key))
                      }
                      className={`rounded-xl border py-3 text-xs font-bold transition-colors cursor-pointer ${
                        activeExtraMode === ex.key
                          ? 'border-amber-400 bg-amber-100 text-amber-950'
                          : 'border-border bg-canvas text-ink hover:bg-surface-muted'
                      }`}
                    >
                      {ex.label}
                    </button>
                  ))}

                  <button
                    type="button"
                    disabled={submitting}
                    onClick={openWicketModal}
                    className="col-span-2 sm:col-span-1 rounded-xl bg-live py-3 text-xs font-extrabold uppercase tracking-wider text-white shadow-sm hover:opacity-90 transition-opacity cursor-pointer"
                  >
                    Wicket!
                  </button>
                </div>

                {/* Quick Extra Run Selector Bar */}
                {activeExtraMode && (
                  <div className="rounded-xl border border-amber-200 bg-amber-50/80 p-4 space-y-2.5 animate-in fade-in-50">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-amber-950">
                        Select {activeExtraMode.replace('_', ' ')} Runs:
                      </span>
                      <button
                        type="button"
                        onClick={() => setActiveExtraMode(null)}
                        className="text-xs font-semibold text-amber-800 hover:underline cursor-pointer"
                      >
                        Cancel
                      </button>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {activeExtraMode === 'WIDE' &&
                        [1, 2, 3, 5].map((totalExtra) => (
                          <button
                            key={totalExtra}
                            type="button"
                            disabled={submitting}
                            onClick={() =>
                              recordBall({
                                batterRuns: 0,
                                extraType: 'WIDE',
                                extraRuns: totalExtra,
                                isWicket: false,
                              })
                            }
                            className="rounded-xl border border-amber-300 bg-white px-4 py-2 text-xs font-bold text-amber-950 hover:bg-amber-100 cursor-pointer"
                          >
                            {totalExtra === 1 ? 'Wide (1)' : `Wide + ${totalExtra - 1} (${totalExtra})`}
                          </button>
                        ))}

                      {activeExtraMode === 'NO_BALL' &&
                        [0, 1, 2, 4, 6].map((batRuns) => (
                          <button
                            key={batRuns}
                            type="button"
                            disabled={submitting}
                            onClick={() =>
                              recordBall({
                                batterRuns: batRuns,
                                extraType: 'NO_BALL',
                                extraRuns: 1,
                                isWicket: false,
                              })
                            }
                            className="rounded-xl border border-amber-300 bg-white px-4 py-2 text-xs font-bold text-amber-950 hover:bg-amber-100 cursor-pointer"
                          >
                            {batRuns === 0 ? 'NB (1)' : `NB + ${batRuns} bat (${batRuns + 1})`}
                          </button>
                        ))}

                      {(activeExtraMode === 'BYE' || activeExtraMode === 'LEG_BYE') &&
                        [1, 2, 3, 4].map((exRuns) => (
                          <button
                            key={exRuns}
                            type="button"
                            disabled={submitting}
                            onClick={() =>
                              recordBall({
                                batterRuns: 0,
                                extraType: activeExtraMode,
                                extraRuns: exRuns,
                                isWicket: false,
                              })
                            }
                            className="rounded-xl border border-amber-300 bg-white px-4 py-2 text-xs font-bold text-amber-950 hover:bg-amber-100 cursor-pointer"
                          >
                            {activeExtraMode === 'BYE' ? `Bye ${exRuns}` : `Leg Bye ${exRuns}`}
                          </button>
                        ))}
                    </div>
                  </div>
                )}
              </section>
            )}
        </>
      )}

      {/* Wicket Modal */}
      {wicketModalOpen && activeInnings && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md rounded-2xl border border-border bg-surface p-6 shadow-xl">
            <h3 className="text-lg font-extrabold text-ink pb-3 border-b border-border">
              Record Wicket (Wicket #{activeInnings.wickets + 1})
            </h3>
            <form onSubmit={handleConfirmWicket} className="mt-4 space-y-4 text-xs">
              <div>
                <label className="block font-bold uppercase tracking-wider text-ink-secondary mb-1">
                  Dismissal Mode
                </label>
                <select
                  value={dismissalType}
                  onChange={(e) => setDismissalType(e.target.value)}
                  className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                >
                  {DISMISSAL_OPTIONS.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-bold uppercase tracking-wider text-ink-secondary mb-1">
                  Dismissed Batter
                </label>
                <select
                  value={dismissedPlayerId}
                  onChange={(e) => setDismissedPlayerId(e.target.value)}
                  className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                >
                  <option value={activeInnings.currentStrikerId}>
                    {activeInnings.currentStrikerName} (Striker)
                  </option>
                  {dismissalType === 'RUN_OUT' && (
                    <option value={activeInnings.currentNonStrikerId}>
                      {activeInnings.currentNonStrikerName} (Non-Striker)
                    </option>
                  )}
                </select>
              </div>

              {['CAUGHT', 'RUN_OUT', 'STUMPED'].includes(dismissalType) && (
                <div>
                  <label className="block font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Fielder / Wicketkeeper
                  </label>
                  <select
                    value={fielderId}
                    onChange={(e) => setFielderId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value="">-- Optional / Select Fielder --</option>
                    {bowlingXI.map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {dismissalType === 'RUN_OUT' && (
                <div>
                  <label className="block font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Completed Runs Before Run Out
                  </label>
                  <select
                    value={wicketRunOutRuns}
                    onChange={(e) => setWicketRunOutRuns(Number(e.target.value))}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    {[0, 1, 2, 3].map((r) => (
                      <option key={r} value={r}>
                        {r} Run{r === 1 ? '' : 's'}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {activeInnings.wickets < 9 && (
                <div>
                  <label className="block font-bold uppercase tracking-wider text-ink-secondary mb-1">
                    Next Incoming Batter *
                  </label>
                  <select
                    required
                    value={nextBatterPlayerId}
                    onChange={(e) => setNextBatterPlayerId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value="">-- Select Next Batter --</option>
                    {availableNextBatters.map((p) => (
                      <option key={p.playerId} value={p.playerId}>
                        {p.playerName} ({p.playingRole?.replace('_', ' ')})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div className="flex justify-end gap-2.5 pt-3 border-t border-border">
                <button
                  type="button"
                  onClick={() => setWicketModalOpen(false)}
                  className="rounded-xl border border-border bg-surface px-4 py-2 font-semibold text-ink hover:bg-surface-muted cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="rounded-xl bg-live px-5 py-2 font-bold text-white shadow-xs hover:opacity-90 cursor-pointer"
                >
                  Confirm Wicket
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
