import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertCircle,
  ArrowRight,
  Calendar,
  CheckCircle2,
  Plus,
  RefreshCw,
  Shield,
  Trophy,
  Users,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import MatchCard from '../matches/MatchCard';
import SquadManagementModal from '../squads/SquadManagementModal';
import AddPlayerModal from '../players/AddPlayerModal';
import { useAuth } from '../../context/AuthContext';
import { matchApi, playerApi, squadApi, teamApi, tournamentApi } from '../../services/apiClient';
import { getTeamInitials, getTeamMonogramClass } from '../../utils/teamHelpers';

export default function TeamCaptainDashboard() {
  const { roleAssignment } = useAuth();
  const teamId = roleAssignment?.teamId;

  const [team, setTeam] = useState(null);
  const [players, setPlayers] = useState([]);
  const [registeredTournaments, setRegisteredTournaments] = useState([]);
  const [teamMatches, setTeamMatches] = useState([]);
  const [scorecardsByMatchId, setScorecardsByMatchId] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [addPlayerOpen, setAddPlayerOpen] = useState(false);
  const [squadModalOpen, setSquadModalOpen] = useState(false);
  const [selectedTournament, setSelectedTournament] = useState(null);

  const loadCaptainData = useCallback(async () => {
    if (!teamId) return;
    setLoading(true);
    setError('');
    try {
      const [teamData, playerList, allTourneys, allMatches] = await Promise.all([
        teamApi.getById(teamId),
        playerApi.getByTeam(teamId),
        tournamentApi.getAll().catch(() => []),
        matchApi.getAll().catch(() => []),
      ]);

      setTeam(teamData);
      setPlayers(Array.isArray(playerList) ? playerList : []);

      // Scope tournament checks to captain's college & load squad size per registration
      const safeTourneys = (Array.isArray(allTourneys) ? allTourneys : []).filter(
        (t) =>
          t.status !== 'DRAFT' &&
          (!teamData?.collegeId || !t.collegeId || String(t.collegeId) === String(teamData.collegeId))
      );

      const regChecks = await Promise.allSettled(
        safeTourneys.map(async (t) => {
          const teamsInT = await tournamentApi.getTeams(t.id);
          const isReg =
            Array.isArray(teamsInT) &&
            teamsInT.some((reg) => String(reg.teamId) === String(teamId));
          if (!isReg) return null;
          const squadRes = await squadApi.getSquad(t.id, teamId).catch(() => null);
          const squadCount = Array.isArray(squadRes?.members) ? squadRes.members.length : 0;
          return { ...t, squadCount };
        })
      );
      const myRegTourneys = regChecks
        .filter((r) => r.status === 'fulfilled' && r.value)
        .map((r) => r.value);
      setRegisteredTournaments(myRegTourneys);

      // Filter matches involving captain's team & fetch scorecards
      const myMatches = (Array.isArray(allMatches) ? allMatches : [])
        .filter(
          (m) => String(m.teamAId) === String(teamId) || String(m.teamBId) === String(teamId)
        )
        .sort((a, b) => new Date(b.matchDateTime) - new Date(a.matchDateTime));
      setTeamMatches(myMatches);

      const scorecardTargets = myMatches.filter(
        (m) =>
          m.status === 'IN_PROGRESS' ||
          m.status === 'COMPLETED' ||
          m.status === 'ABANDONED'
      );
      if (scorecardTargets.length > 0) {
        const settled = await Promise.allSettled(
          scorecardTargets.map((m) => matchApi.getScorecard(m.id))
        );
        const map = {};
        settled.forEach((res, idx) => {
          if (res.status === 'fulfilled' && res.value) {
            map[scorecardTargets[idx].id] = res.value;
          }
        });
        setScorecardsByMatchId(map);
      } else {
        setScorecardsByMatchId({});
      }
    } catch (err) {
      setError(err.message || 'Failed to load team captain portal data.');
    } finally {
      setLoading(false);
    }
  }, [teamId]);

  useEffect(() => {
    loadCaptainData();
  }, [loadCaptainData]);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="flex items-center gap-4">
            <div
              className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl font-black text-xl shadow-sm ${getTeamMonogramClass(
                team?.name || 'My Team'
              )}`}
            >
              {getTeamInitials(team?.name || 'TM', team?.shortName)}
            </div>
            <div>
              <div className="inline-flex items-center gap-1.5 rounded-full bg-brand-soft px-3 py-0.5 text-xs font-bold text-brand-hover mb-1">
                <Shield className="h-3.5 w-3.5" />
                Team Captain
              </div>
              <h1 className="text-2xl font-extrabold tracking-tight text-ink">
                {team?.name || 'My Team'}
              </h1>
              <p className="text-xs text-ink-secondary">
                College: <span className="font-semibold text-ink">{team?.collegeName || '—'}</span> • Home: {team?.homeGround || 'Ground TBD'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <button
              type="button"
              onClick={loadCaptainData}
              className="inline-flex items-center gap-1.5 rounded-xl border border-border bg-canvas px-3.5 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors cursor-pointer"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
            {teamId && (
              <Link
                to={`/teams/${teamId}`}
                className="inline-flex items-center gap-1.5 rounded-xl border border-brand-soft bg-surface-mint px-4 py-2 text-xs font-bold text-brand-hover hover:bg-brand-soft transition-colors"
              >
                <Shield className="h-3.5 w-3.5" />
                Team Profile
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

      {/* Tournament Registrations & Squad Selection */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-pitch-soft text-pitch">
              <Trophy className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Tournament Squad Management</h2>
              <p className="text-xs text-ink-secondary">Select up to 15 players per registered tournament</p>
            </div>
          </div>
          <Link
            to="/tournaments"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>Browse Tournaments</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4">
          {registeredTournaments.length === 0 ? (
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 rounded-xl border border-dashed border-border bg-canvas/60 p-5">
              <div className="space-y-1">
                <p className="text-sm font-bold text-ink">Your team is not registered in any tournament yet</p>
                <p className="text-xs text-ink-secondary">
                  Browse published tournaments to register your team, lock your 15-player squad, and play fixtures.
                </p>
              </div>
              <Link
                to="/tournaments"
                className="shrink-0 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors"
              >
                Register in Tournaments
              </Link>
            </div>
          ) : (
            <div className="space-y-3">
              {registeredTournaments.map((t) => (
                <div
                  key={t.id}
                  className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 rounded-xl border border-border bg-canvas p-4 text-xs"
                >
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-bold text-ink text-sm">{t.name}</span>
                      <StatusBadge status={t.status} />
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-[11px] font-bold ${
                          t.squadCount >= 11
                            ? 'bg-pitch-soft text-pitch'
                            : 'bg-boundary-soft text-boundary'
                        }`}
                      >
                        {t.squadCount} / 15 Squad Players
                      </span>
                    </div>
                    <p className="text-ink-secondary">
                      Format: {t.format} • Starts: {t.startDate}
                    </p>
                  </div>

                  <div className="flex items-center gap-2 sm:self-center">
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedTournament(t);
                        setSquadModalOpen(true);
                      }}
                      className="inline-flex items-center gap-1.5 rounded-xl bg-brand px-4 py-2 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
                    >
                      <Users className="h-3.5 w-3.5" />
                      Manage Squad ({t.squadCount}/15)
                    </button>
                    <Link
                      to={`/tournaments/${t.id}`}
                      className="rounded-xl border border-border bg-surface px-3 py-2 text-xs font-semibold text-ink hover:bg-surface-muted transition-colors"
                    >
                      View Tournament
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Team Fixtures & Results */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-surface-mint text-brand">
              <Calendar className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Team Fixtures &amp; Results</h2>
              <p className="text-xs text-ink-secondary">
                {teamMatches.length} scheduled or completed match(es) for {team?.name || 'your team'}
              </p>
            </div>
          </div>
          <Link
            to="/matches"
            className="inline-flex items-center gap-1 text-xs font-bold text-brand hover:text-brand-hover"
          >
            <span>All Matches</span>
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>

        <div className="mt-4">
          {teamMatches.length === 0 ? (
            <div className="rounded-xl border border-dashed border-border bg-canvas/60 p-6 text-center text-xs text-ink-muted">
              No fixtures scheduled for your team yet.
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              {teamMatches.slice(0, 4).map((m) => (
                <MatchCard
                  key={m.id}
                  match={m}
                  scorecard={scorecardsByMatchId[m.id]}
                />
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Permanent Player Roster */}
      <section className="rounded-2xl border border-border bg-surface p-6 shadow-sm">
        <div className="flex items-center justify-between pb-4 border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-soft text-brand-hover">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-base font-extrabold text-ink">Permanent Player Roster</h2>
              <p className="text-xs text-ink-secondary">{players.length} registered player(s) in your club</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setAddPlayerOpen(true)}
            className="inline-flex items-center gap-1.5 rounded-full bg-brand px-3 py-1 text-xs font-bold text-white shadow-sm hover:bg-brand-hover transition-colors cursor-pointer"
          >
            <Plus className="h-3.5 w-3.5" />
            Add Player
          </button>
        </div>

        <div className="mt-4">
          {players.length === 0 ? (
            <div className="py-8 text-center text-xs text-ink-muted border border-dashed border-border rounded-xl">
              No players added yet. Click "Add Player" to enroll players into your team.
            </div>
          ) : (
            <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-3">
              {players.map((p) => (
                <div
                  key={p.id}
                  className="rounded-xl border border-border bg-canvas p-3 text-xs flex items-center justify-between"
                >
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-1.5">
                      <span className="font-bold text-ink">{p.fullName}</span>
                      {p.jerseyNumber != null && (
                        <span className="rounded bg-surface-muted px-1.5 py-0.2 text-[10px] font-bold text-ink-secondary">
                          #{p.jerseyNumber}
                        </span>
                      )}
                    </div>
                    <p className="text-[11px] text-ink-secondary">
                      {p.playingRole?.replace('_', ' ') || 'Player'}
                    </p>
                  </div>
                  <span
                    className={`rounded-full px-2 py-0.5 text-[10px] font-bold ${
                      p.active
                        ? 'bg-pitch-soft text-pitch'
                        : 'bg-surface-muted text-ink-muted'
                    }`}
                  >
                    {p.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      {/* Add Player Modal */}
      <AddPlayerModal
        open={addPlayerOpen}
        onClose={() => setAddPlayerOpen(false)}
        teamId={teamId}
        onPlayerAdded={(newPlayer) => {
          setFeedback(`Player ${newPlayer.fullName} added to team roster.`);
          loadCaptainData();
        }}
      />

      {/* Squad Management Modal */}
      {selectedTournament && (
        <SquadManagementModal
          open={squadModalOpen}
          onClose={() => {
            setSquadModalOpen(false);
            setSelectedTournament(null);
          }}
          tournamentId={selectedTournament.id}
          teamId={teamId}
          tournamentName={selectedTournament.name}
          teamName={team?.name}
          onSquadSaved={() => {
            setFeedback('Tournament squad selection updated.');
            loadCaptainData();
          }}
        />
      )}
    </div>
  );
}
