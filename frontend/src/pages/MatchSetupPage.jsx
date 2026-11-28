import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  AlertCircle,
  ArrowLeft,
  ArrowRight,
  Check,
  CheckCircle2,
  Coins,
  Loader2,
  Play,
  RefreshCw,
  Shield,
  Users,
  Zap,
} from 'lucide-react';
import AuthRequiredCard from '../components/common/AuthRequiredCard';
import StatusBadge from '../components/common/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { matchApi, playerApi, squadApi } from '../services/apiClient';
import { canManageMatches } from '../utils/matchHelpers';

function TeamPlayingXICard({
  teamId,
  teamName,
  eligiblePlayers,
  savedXI,
  otherTeamSelectedIds,
  canEdit,
  onSaveXI,
  saving,
}) {
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [captainId, setCaptainId] = useState('');
  const [wkId, setWkId] = useState('');
  const [localError, setLocalError] = useState('');

  // Sync from saved Playing XI when loaded
  useEffect(() => {
    if (savedXI && savedXI.length > 0) {
      setSelectedIds(new Set(savedXI.map((p) => p.playerId)));
      const cap = savedXI.find((p) => p.captain);
      const wk = savedXI.find((p) => p.wicketkeeper);
      setCaptainId(cap ? String(cap.playerId) : String(savedXI[0].playerId));
      setWkId(wk ? String(wk.playerId) : String(savedXI[0].playerId));
    } else {
      setSelectedIds(new Set());
      setCaptainId('');
      setWkId('');
    }
  }, [savedXI]);

  const togglePlayer = (playerId) => {
    if (!canEdit) return;
    setLocalError('');
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(playerId)) {
        next.delete(playerId);
        if (String(playerId) === captainId) setCaptainId('');
        if (String(playerId) === wkId) setWkId('');
      } else {
        if (next.size >= 11) {
          setLocalError('Playing XI can only have 11 players. Uncheck someone first.');
          return prev;
        }
        next.add(playerId);
      }
      return next;
    });
  };

  const handleQuickSelect11 = () => {
    if (!canEdit) return;
    setLocalError('');
    const available = eligiblePlayers.filter((p) => !otherTeamSelectedIds.has(p.playerId));
    const first11 = available.slice(0, 11);
    if (first11.length < 11) {
      setLocalError(`Only ${first11.length} eligible players available; need 11.`);
      return;
    }
    setSelectedIds(new Set(first11.map((p) => p.playerId)));
    const defaultCap = first11[0];
    const defaultWk =
      first11.find((p) => p.playingRole === 'WICKET_KEEPER') || first11[1] || first11[0];
    setCaptainId(String(defaultCap.playerId));
    setWkId(String(defaultWk.playerId));
  };

  const selectedList = useMemo(
    () => eligiblePlayers.filter((p) => selectedIds.has(p.playerId)),
    [eligiblePlayers, selectedIds]
  );

  // Auto-select captain/wk when 11 are checked if empty
  useEffect(() => {
    if (selectedList.length === 11) {
      if (!captainId || !selectedIds.has(Number(captainId))) {
        setCaptainId(String(selectedList[0].playerId));
      }
      if (!wkId || !selectedIds.has(Number(wkId))) {
        const wk =
          selectedList.find((p) => p.playingRole === 'WICKET_KEEPER') ||
          selectedList[1] ||
          selectedList[0];
        setWkId(String(wk.playerId));
      }
    }
  }, [selectedList, captainId, wkId, selectedIds]);

  const handleSave = () => {
    setLocalError('');
    if (selectedIds.size !== 11) {
      setLocalError(`Please select exactly 11 players (currently ${selectedIds.size}/11).`);
      return;
    }
    if (!captainId) {
      setLocalError('Please choose a match Captain from the 11 players.');
      return;
    }
    if (!wkId) {
      setLocalError('Please choose a Wicketkeeper from the 11 players.');
      return;
    }
    onSaveXI({
      teamId,
      playerIds: Array.from(selectedIds),
      captainPlayerId: Number(captainId),
      wicketkeeperPlayerId: Number(wkId),
    });
  };

  const isSaved = savedXI && savedXI.length === 11;

  return (
    <div className="rounded-2xl border border-border bg-surface p-5 shadow-xs flex flex-col justify-between space-y-4">
      <div>
        <div className="flex items-start justify-between gap-3 pb-3 border-b border-border">
          <div>
            <div className="flex items-center gap-2">
              <h3 className="text-base font-extrabold text-ink">{teamName}</h3>
              {isSaved ? (
                <span className="inline-flex items-center gap-1 rounded-full bg-pitch-soft px-2 py-0.5 text-[10px] font-bold text-pitch">
                  <CheckCircle2 className="h-3 w-3" />
                  11/11 Ready
                </span>
              ) : (
                <span className="rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-800">
                  {selectedIds.size}/11 Selected
                </span>
              )}
            </div>
            <p className="text-xs text-ink-secondary mt-0.5">
              {eligiblePlayers.length} eligible player(s) available
            </p>
          </div>

          {canEdit && eligiblePlayers.length >= 11 && (
            <button
              type="button"
              onClick={handleQuickSelect11}
              className="inline-flex items-center gap-1 rounded-lg border border-border bg-canvas px-2.5 py-1 text-[11px] font-semibold text-brand-hover hover:bg-brand-soft/50 transition-colors cursor-pointer"
            >
              <Zap className="h-3 w-3" />
              Auto-Pick 11
            </button>
          )}
        </div>

        {localError && (
          <div className="mt-3 flex items-start gap-2 rounded-xl border border-live-soft bg-live-soft/60 p-2.5 text-xs text-live">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
            <span>{localError}</span>
          </div>
        )}

        {eligiblePlayers.length === 0 ? (
          <div className="mt-4 rounded-xl border border-dashed border-border p-6 text-center text-xs text-ink-muted">
            No players found in squad or active team roster. Add players first.
          </div>
        ) : (
          <div className="mt-3 max-h-72 overflow-y-auto space-y-1.5 pr-1">
            {eligiblePlayers.map((p) => {
              const checked = selectedIds.has(p.playerId);
              const disabledOpponent = otherTeamSelectedIds.has(p.playerId);
              return (
                <label
                  key={p.playerId}
                  className={`flex items-center justify-between gap-2 rounded-xl border p-2.5 text-xs transition-colors ${
                    disabledOpponent
                      ? 'opacity-45 cursor-not-allowed border-border bg-canvas'
                      : checked
                        ? 'border-brand/50 bg-brand-soft/30 cursor-pointer'
                        : canEdit
                          ? 'border-border bg-canvas hover:bg-surface-muted/50 cursor-pointer'
                          : 'border-border bg-canvas'
                  }`}
                >
                  <div className="flex items-center gap-2.5 min-w-0">
                    <div
                      className={`flex h-4.5 w-4.5 shrink-0 items-center justify-center rounded border ${
                        checked
                          ? 'border-brand bg-brand text-white'
                          : 'border-border bg-surface'
                      }`}
                    >
                      {checked && <Check className="h-3 w-3" />}
                    </div>
                    <input
                      type="checkbox"
                      checked={checked}
                      disabled={!canEdit || disabledOpponent}
                      onChange={() => togglePlayer(p.playerId)}
                      className="sr-only"
                    />
                    <div className="min-w-0">
                      <p className="font-bold text-ink truncate">
                        {p.fullName}{' '}
                        {p.jerseyNumber != null && (
                          <span className="text-ink-muted font-normal">#{p.jerseyNumber}</span>
                        )}
                      </p>
                      <p className="text-[11px] text-ink-secondary">
                        {p.playingRole?.replace('_', ' ') || 'Player'}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1 shrink-0">
                    {String(p.playerId) === captainId && checked && (
                      <span className="rounded bg-brand px-1.5 py-0.5 text-[10px] font-bold text-white">
                        C
                      </span>
                    )}
                    {String(p.playerId) === wkId && checked && (
                      <span className="rounded bg-pitch px-1.5 py-0.5 text-[10px] font-bold text-white">
                        WK
                      </span>
                    )}
                  </div>
                </label>
              );
            })}
          </div>
        )}

        {selectedList.length > 0 && canEdit && (
          <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-3 border-t border-border/60">
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-ink-secondary mb-1">
                Match Captain (C)
              </label>
              <select
                value={captainId}
                onChange={(e) => setCaptainId(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-2.5 py-2 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
              >
                <option value="">-- Select Captain --</option>
                {selectedList.map((p) => (
                  <option key={p.playerId} value={p.playerId}>
                    {p.fullName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-[10px] font-bold uppercase tracking-wider text-ink-secondary mb-1">
                Wicketkeeper (WK)
              </label>
              <select
                value={wkId}
                onChange={(e) => setWkId(e.target.value)}
                className="w-full rounded-xl border border-border bg-canvas px-2.5 py-2 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
              >
                <option value="">-- Select WK --</option>
                {selectedList.map((p) => (
                  <option key={p.playerId} value={p.playerId}>
                    {p.fullName}
                  </option>
                ))}
              </select>
            </div>
          </div>
        )}
      </div>

      {canEdit && (
        <button
          type="button"
          onClick={handleSave}
          disabled={saving || selectedIds.size !== 11}
          className="w-full inline-flex items-center justify-center gap-1.5 rounded-xl bg-brand py-2.5 text-xs font-bold text-white shadow-xs hover:bg-brand-hover disabled:opacity-50 transition-colors cursor-pointer"
        >
          {saving ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Users className="h-3.5 w-3.5" />}
          {saving ? 'Saving Playing XI...' : isSaved ? 'Update Playing XI' : 'Save Playing XI (11)'}
        </button>
      )}
    </div>
  );
}

export default function MatchSetupPage() {
  const { matchId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, roleAssignment } = useAuth();

  const canManageAll = isAuthenticated && canManageMatches(roleAssignment);
  const myTeamId = roleAssignment?.role === 'TEAM_CAPTAIN' ? roleAssignment?.teamId : null;

  const [match, setMatch] = useState(null);
  const [teamAPlayers, setTeamAPlayers] = useState([]);
  const [teamBPlayers, setTeamBPlayers] = useState([]);
  const [savedXI, setSavedXI] = useState({ teamAPlayers: [], teamBPlayers: [] });
  const [toss, setToss] = useState(null);
  const [inningsList, setInningsList] = useState([]);

  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [savingTeamId, setSavingTeamId] = useState(null);
  const [savingToss, setSavingToss] = useState(false);
  const [startingInnings, setStartingInnings] = useState(false);

  // Toss form state
  const [tossWinnerTeamId, setTossWinnerTeamId] = useState('');
  const [tossDecision, setTossDecision] = useState('BAT');

  // Start Innings 1 state
  const [strikerId, setStrikerId] = useState('');
  const [nonStrikerId, setNonStrikerId] = useState('');
  const [bowlerId, setBowlerId] = useState('');

  const normalizeEligible = (squadRes, rosterList) => {
    if (squadRes && Array.isArray(squadRes.members) && squadRes.members.length > 0) {
      return squadRes.members.map((m) => ({
        playerId: m.playerId,
        fullName: m.playerName,
        jerseyNumber: m.jerseyNumber,
        playingRole: m.playingRole,
      }));
    }
    return (Array.isArray(rosterList) ? rosterList : []).map((p) => ({
      playerId: p.id,
      fullName: p.fullName,
      jerseyNumber: p.jerseyNumber,
      playingRole: p.playingRole,
    }));
  };

  const loadSetupData = useCallback(async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const mData = await matchApi.getById(matchId);
      setMatch(mData);

      const [xiRes, tossRes, innRes, sqA, sqB, rosA, rosB] = await Promise.all([
        matchApi.getPlayingXI(matchId).catch(() => ({ teamAPlayers: [], teamBPlayers: [] })),
        matchApi.getToss(matchId).catch(() => null),
        matchApi.getInnings(matchId).catch(() => []),
        mData.tournamentId
          ? squadApi.getSquad(mData.tournamentId, mData.teamAId).catch(() => null)
          : Promise.resolve(null),
        mData.tournamentId
          ? squadApi.getSquad(mData.tournamentId, mData.teamBId).catch(() => null)
          : Promise.resolve(null),
        playerApi.getActiveByTeam(mData.teamAId).catch(() => []),
        playerApi.getActiveByTeam(mData.teamBId).catch(() => []),
      ]);

      setTeamAPlayers(normalizeEligible(sqA, rosA));
      setTeamBPlayers(normalizeEligible(sqB, rosB));
      setSavedXI(xiRes || { teamAPlayers: [], teamBPlayers: [] });
      setToss(tossRes);
      setInningsList(Array.isArray(innRes) ? innRes : []);

      if (tossRes?.tossWinnerTeamId) {
        setTossWinnerTeamId(String(tossRes.tossWinnerTeamId));
        setTossDecision(tossRes.decision || 'BAT');
      } else {
        setTossWinnerTeamId(String(mData.teamAId));
      }
    } catch (err) {
      setError(err.message || 'Failed to load match setup details.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, matchId]);

  useEffect(() => {
    loadSetupData();
  }, [loadSetupData]);

  const handleSaveXI = async (payload) => {
    setSavingTeamId(payload.teamId);
    setError('');
    setFeedback('');
    try {
      const updatedXI = await matchApi.setPlayingXI(matchId, payload);
      setSavedXI(updatedXI);
      setFeedback('Playing XI saved successfully!');
    } catch (err) {
      setError(err.message || 'Failed to save Playing XI.');
    } finally {
      setSavingTeamId(null);
    }
  };

  const handleSaveToss = async (e) => {
    e.preventDefault();
    if (!tossWinnerTeamId) return;
    setSavingToss(true);
    setError('');
    setFeedback('');
    try {
      const res = await matchApi.recordToss(matchId, {
        tossWinnerTeamId: Number(tossWinnerTeamId),
        decision: tossDecision,
      });
      setToss(res);
      setFeedback('Toss result recorded!');
    } catch (err) {
      setError(err.message || 'Failed to record toss.');
    } finally {
      setSavingToss(false);
    }
  };

  // Compute batting and bowling teams for Innings 1 from recorded toss
  const { battingFirstTeam, bowlingFirstTeam } = useMemo(() => {
    if (!match || !toss?.tossWinnerTeamId) {
      return { battingFirstTeam: null, bowlingFirstTeam: null };
    }
    const winnerIsA = String(toss.tossWinnerTeamId) === String(match.teamAId);
    const teamA = {
      id: match.teamAId,
      name: match.teamAName,
      players: savedXI?.teamAPlayers || [],
    };
    const teamB = {
      id: match.teamBId,
      name: match.teamBName,
      players: savedXI?.teamBPlayers || [],
    };
    const winner = winnerIsA ? teamA : teamB;
    const loser = winnerIsA ? teamB : teamA;

    if (toss.decision === 'BAT') {
      return { battingFirstTeam: winner, bowlingFirstTeam: loser };
    }
    return { battingFirstTeam: loser, bowlingFirstTeam: winner };
  }, [match, toss, savedXI]);

  // Pre-select opening pair & bowler when ready
  useEffect(() => {
    if (battingFirstTeam?.players?.length >= 2) {
      if (!strikerId) setStrikerId(String(battingFirstTeam.players[0].playerId));
      if (!nonStrikerId) setNonStrikerId(String(battingFirstTeam.players[1].playerId));
    }
    if (bowlingFirstTeam?.players?.length >= 1 && !bowlerId) {
      const preferredBowler =
        bowlingFirstTeam.players.find(
          (p) => p.playingRole === 'BOWLER' || p.playingRole === 'ALL_ROUNDER'
        ) || bowlingFirstTeam.players[bowlingFirstTeam.players.length - 1];
      setBowlerId(String(preferredBowler.playerId));
    }
  }, [battingFirstTeam, bowlingFirstTeam, strikerId, nonStrikerId, bowlerId]);

  const handleStartInnings1 = async (e) => {
    e.preventDefault();
    setError('');
    if (!strikerId || !nonStrikerId || !bowlerId) {
      setError('Select Opening Striker, Non-Striker, and Opening Bowler.');
      return;
    }
    if (strikerId === nonStrikerId) {
      setError('Striker and Non-Striker must be different players.');
      return;
    }

    setStartingInnings(true);
    try {
      if (match.status === 'SCHEDULED') {
        const updatedMatch = await matchApi.update(matchId, {
          tournamentId: match.tournamentId,
          teamAId: match.teamAId,
          teamBId: match.teamBId,
          matchDateTime: match.matchDateTime,
          venue: match.venue,
          format: match.format,
          maxOvers: match.maxOvers,
          status: 'IN_PROGRESS',
        });
        setMatch(updatedMatch);
      }
      await matchApi.startInnings(matchId, {
        inningsNumber: 1,
        strikerPlayerId: Number(strikerId),
        nonStrikerPlayerId: Number(nonStrikerId),
        bowlerPlayerId: Number(bowlerId),
      });
      navigate(`/matches/${matchId}/score`);
    } catch (err) {
      setError(err.message || 'Failed to start first innings.');
    } finally {
      setStartingInnings(false);
    }
  };

  const teamAXICount = savedXI?.teamAPlayers?.length || 0;
  const teamBXICount = savedXI?.teamBPlayers?.length || 0;
  const bothXIsReady = teamAXICount === 11 && teamBXICount === 11;
  const isInProgress = match?.status === 'IN_PROGRESS' || inningsList.length > 0;
  const isCompleted = match?.status === 'COMPLETED';

  const teamAOppSet = useMemo(
    () => new Set((savedXI?.teamBPlayers || []).map((p) => p.playerId)),
    [savedXI]
  );
  const teamBOppSet = useMemo(
    () => new Set((savedXI?.teamAPlayers || []).map((p) => p.playerId)),
    [savedXI]
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <Link
          to="/matches"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-ink-secondary hover:text-ink transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Matches
        </Link>
        {isAuthenticated && (
          <button
            type="button"
            onClick={loadSetupData}
            disabled={loading}
            className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-surface px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted cursor-pointer"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        )}
      </div>

      {!isAuthenticated ? (
        <AuthRequiredCard
          title="Sign in to configure match setup"
          description="Match setup, Playing XI selection, and toss recording require an authenticated organizer or captain."
        />
      ) : loading ? (
        <div className="h-64 animate-pulse rounded-2xl border border-border bg-surface p-6" />
      ) : !match ? (
        <div className="rounded-2xl border border-live-soft bg-live-soft/50 p-6 text-sm text-live">
          {error || 'Match not found.'}
        </div>
      ) : (
        <>
          {/* Match Header */}
          <section className="rounded-2xl border border-border bg-surface p-6 shadow-[var(--shadow-card)]">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <div className="flex items-center gap-2">
                  <StatusBadge status={match.status} />
                  <span className="text-xs font-semibold text-brand-hover">
                    {match.tournamentName || 'Tournament Match'}
                  </span>
                </div>
                <h1 className="mt-2 text-2xl font-extrabold tracking-tight text-ink">
                  {match.teamAName} vs {match.teamBName}
                </h1>
                <p className="mt-1 text-xs text-ink-secondary">
                  {match.venue || 'Venue TBD'} • {match.format} ({match.maxOvers} Overs)
                </p>
              </div>

              <div className="flex items-center gap-2.5">
                <Link
                  to={`/matches/${matchId}/scorecard`}
                  className="rounded-xl border border-border bg-canvas px-4 py-2 text-xs font-semibold text-ink hover:bg-surface-muted"
                >
                  View Scorecard
                </Link>
                {isInProgress && !isCompleted && (
                  <Link
                    to={`/matches/${matchId}/score`}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-live px-4 py-2 text-xs font-bold text-white shadow-sm hover:opacity-90"
                  >
                    <Play className="h-3.5 w-3.5" />
                    Resume Live Scorer →
                  </Link>
                )}
              </div>
            </div>
          </section>

          {feedback && (
            <div className="flex items-start gap-2.5 rounded-xl border border-pitch-soft bg-pitch-soft/70 p-3.5 text-xs text-pitch font-semibold">
              <CheckCircle2 className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{feedback}</span>
            </div>
          )}

          {error && (
            <div className="flex items-start gap-2.5 rounded-xl border border-live-soft bg-live-soft/70 p-3.5 text-xs text-live">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Step 1: Playing XI Nomination */}
          <section className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand text-xs font-black text-white">
                  1
                </span>
                <h2 className="text-lg font-extrabold text-ink">Select Playing XI (11 Players Each)</h2>
              </div>
              {bothXIsReady && (
                <span className="inline-flex items-center gap-1 rounded-full bg-pitch-soft px-3 py-0.5 text-xs font-bold text-pitch">
                  <CheckCircle2 className="h-3.5 w-3.5" />
                  Both XIs Confirmed
                </span>
              )}
            </div>

            <div className="grid gap-5 lg:grid-cols-2">
              <TeamPlayingXICard
                teamId={match.teamAId}
                teamName={match.teamAName}
                eligiblePlayers={teamAPlayers}
                savedXI={savedXI?.teamAPlayers}
                otherTeamSelectedIds={teamAOppSet}
                canEdit={
                  match.status === 'SCHEDULED' &&
                  (canManageAll || String(myTeamId) === String(match.teamAId))
                }
                onSaveXI={handleSaveXI}
                saving={savingTeamId === match.teamAId}
              />

              <TeamPlayingXICard
                teamId={match.teamBId}
                teamName={match.teamBName}
                eligiblePlayers={teamBPlayers}
                savedXI={savedXI?.teamBPlayers}
                otherTeamSelectedIds={teamBOppSet}
                canEdit={
                  match.status === 'SCHEDULED' &&
                  (canManageAll || String(myTeamId) === String(match.teamBId))
                }
                onSaveXI={handleSaveXI}
                saving={savingTeamId === match.teamBId}
              />
            </div>
          </section>

          {/* Step 2: Toss Recording */}
          <section className="rounded-2xl border border-border bg-surface p-6 shadow-xs space-y-4">
            <div className="flex items-center justify-between border-b border-border pb-3">
              <div className="flex items-center gap-2">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand text-xs font-black text-white">
                  2
                </span>
                <div>
                  <h2 className="text-lg font-extrabold text-ink">Record Toss</h2>
                  <p className="text-xs text-ink-secondary">
                    Confirm toss winner and whether they elected to bat or bowl first
                  </p>
                </div>
              </div>
              {toss?.tossWinnerTeamId && (
                <span className="inline-flex items-center gap-1 rounded-full bg-pitch-soft px-3 py-0.5 text-xs font-bold text-pitch">
                  <Coins className="h-3.5 w-3.5" />
                  Toss Recorded
                </span>
              )}
            </div>

            {canManageAll && match.status === 'SCHEDULED' ? (
              <form onSubmit={handleSaveToss} className="grid gap-4 sm:grid-cols-3 items-end">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                    Toss Won By
                  </label>
                  <select
                    value={tossWinnerTeamId}
                    onChange={(e) => setTossWinnerTeamId(e.target.value)}
                    className="w-full rounded-xl border border-border bg-canvas px-3.5 py-2.5 text-sm font-semibold text-ink focus:border-brand focus:outline-none"
                  >
                    <option value={match.teamAId}>{match.teamAName}</option>
                    <option value={match.teamBId}>{match.teamBName}</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                    Elected To
                  </label>
                  <div className="grid grid-cols-2 gap-2">
                    {['BAT', 'BOWL'].map((dec) => (
                      <button
                        key={dec}
                        type="button"
                        onClick={() => setTossDecision(dec)}
                        className={`rounded-xl border py-2.5 text-xs font-bold transition-colors cursor-pointer ${
                          tossDecision === dec
                            ? 'border-brand bg-brand-soft text-brand-hover'
                            : 'border-border bg-canvas text-ink-secondary hover:text-ink'
                        }`}
                      >
                        {dec === 'BAT' ? 'Bat First' : 'Bowl First'}
                      </button>
                    ))}
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={savingToss}
                  className="inline-flex items-center justify-center gap-1.5 rounded-xl bg-brand px-5 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-brand-hover disabled:opacity-60 cursor-pointer"
                >
                  {savingToss && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
                  {toss?.tossWinnerTeamId ? 'Update Toss' : 'Save Toss Result'}
                </button>
              </form>
            ) : (
              <div className="text-sm font-medium text-ink">
                {toss?.tossWinnerTeamId
                  ? `${toss.tossWinnerTeamName} won the toss and elected to ${toss.decision?.toLowerCase()}.`
                  : 'Awaiting toss recording by College Admin.'}
              </div>
            )}
          </section>

          {/* Step 3: Start Innings 1 */}
          {!isInProgress && !isCompleted && canManageAll && (
            <section className="rounded-2xl border border-border bg-surface p-6 shadow-xs space-y-4">
              <div className="flex items-center gap-2 border-b border-border pb-3">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand text-xs font-black text-white">
                  3
                </span>
                <div>
                  <h2 className="text-lg font-extrabold text-ink">Start Innings 1 & Live Scoring</h2>
                  <p className="text-xs text-ink-secondary">
                    Choose the opening batters and opening bowler once both Playing XIs and Toss are saved
                  </p>
                </div>
              </div>

              {!bothXIsReady || !toss?.tossWinnerTeamId ? (
                <div className="rounded-xl border border-dashed border-border bg-canvas p-4 text-xs text-ink-secondary">
                  Complete Step 1 (11 players for both teams) and Step 2 (Record Toss) above to unlock Innings 1 initialization.
                </div>
              ) : (
                <form onSubmit={handleStartInnings1} className="space-y-4">
                  <div className="rounded-xl border border-brand/25 bg-surface-mint p-3.5 text-xs font-semibold text-ink flex flex-wrap gap-4">
                    <span>
                      Batting First:{' '}
                      <strong className="text-brand-hover">{battingFirstTeam?.name}</strong>
                    </span>
                    <span>•</span>
                    <span>
                      Bowling First:{' '}
                      <strong className="text-ink">{bowlingFirstTeam?.name}</strong>
                    </span>
                  </div>

                  <div className="grid gap-4 sm:grid-cols-3">
                    <div>
                      <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                        Opening Striker *
                      </label>
                      <select
                        required
                        value={strikerId}
                        onChange={(e) => setStrikerId(e.target.value)}
                        className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                      >
                        <option value="">-- Select Striker --</option>
                        {battingFirstTeam?.players.map((p) => (
                          <option key={p.playerId} value={p.playerId}>
                            {p.playerName} ({p.playingRole?.replace('_', ' ')})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                        Opening Non-Striker *
                      </label>
                      <select
                        required
                        value={nonStrikerId}
                        onChange={(e) => setNonStrikerId(e.target.value)}
                        className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                      >
                        <option value="">-- Select Non-Striker --</option>
                        {battingFirstTeam?.players.map((p) => (
                          <option key={p.playerId} value={p.playerId}>
                            {p.playerName} ({p.playingRole?.replace('_', ' ')})
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-xs font-bold uppercase tracking-wider text-ink-secondary mb-1.5">
                        Opening Bowler *
                      </label>
                      <select
                        required
                        value={bowlerId}
                        onChange={(e) => setBowlerId(e.target.value)}
                        className="w-full rounded-xl border border-border bg-canvas px-3 py-2.5 text-xs font-semibold text-ink focus:border-brand focus:outline-none"
                      >
                        <option value="">-- Select Bowler --</option>
                        {bowlingFirstTeam?.players.map((p) => (
                          <option key={p.playerId} value={p.playerId}>
                            {p.playerName} ({p.playingRole?.replace('_', ' ')})
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="flex justify-end pt-2">
                    <button
                      type="submit"
                      disabled={startingInnings}
                      className="inline-flex items-center gap-2 rounded-xl bg-pitch px-6 py-3 text-sm font-bold text-white shadow-sm hover:bg-pitch-hover disabled:opacity-60 transition-colors cursor-pointer"
                    >
                      {startingInnings ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        <Play className="h-4 w-4" />
                      )}
                      {startingInnings
                        ? 'Starting Innings...'
                        : 'Start Innings 1 & Open Live Scorer'}
                      <ArrowRight className="h-4 w-4" />
                    </button>
                  </div>
                </form>
              )}
            </section>
          )}
        </>
      )}
    </div>
  );
}
