import { useCallback, useEffect, useMemo, useState } from 'react';
import { matchApi, tournamentApi } from '../services/apiClient';

export function useMatchesWithScorecards(isAuthenticated, selectedCollegeId = '') {
  const [rawMatches, setRawMatches] = useState([]);
  const [collegeByTournamentId, setCollegeByTournamentId] = useState({});
  const [scorecardsByMatchId, setScorecardsByMatchId] = useState({});
  const [loading, setLoading] = useState(Boolean(isAuthenticated));
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!isAuthenticated) {
      setRawMatches([]);
      setCollegeByTournamentId({});
      setScorecardsByMatchId({});
      setLoading(false);
      setError('');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const [matchList, tournamentList] = await Promise.all([
        matchApi.getAll(),
        tournamentApi.getAll().catch(() => []),
      ]);
      const safeMatches = Array.isArray(matchList) ? matchList : [];
      setRawMatches(safeMatches);

      const tourneyMap = {};
      if (Array.isArray(tournamentList)) {
        for (const t of tournamentList) {
          tourneyMap[String(t.id)] = t.collegeId ?? null;
        }
      }
      setCollegeByTournamentId(tourneyMap);

      const targets = safeMatches.filter(
        (m) =>
          m.status === 'IN_PROGRESS' ||
          m.status === 'COMPLETED' ||
          m.status === 'ABANDONED'
      );

      if (targets.length === 0) {
        setScorecardsByMatchId({});
        return;
      }

      const settled = await Promise.allSettled(
        targets.map((m) => matchApi.getScorecard(m.id))
      );

      const map = {};
      settled.forEach((res, idx) => {
        if (res.status === 'fulfilled' && res.value) {
          map[targets[idx].id] = res.value;
        }
      });
      setScorecardsByMatchId(map);
    } catch (err) {
      setError(err.message || 'Failed to load matches.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    load();
  }, [load]);

  const matches = useMemo(() => {
    if (!selectedCollegeId) return rawMatches;
    return rawMatches.filter((m) => {
      if (!m.tournamentId) return true;
      const tCollegeId = collegeByTournamentId[String(m.tournamentId)];
      if (tCollegeId == null) return true;
      return String(tCollegeId) === String(selectedCollegeId);
    });
  }, [rawMatches, selectedCollegeId, collegeByTournamentId]);

  return { matches, scorecardsByMatchId, loading, error, reload: load };
}
