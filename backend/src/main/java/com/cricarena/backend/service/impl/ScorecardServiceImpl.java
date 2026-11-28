package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.response.*;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchDelivery;
import com.cricarena.backend.entity.MatchInnings;
import com.cricarena.backend.entity.MatchPlayingXI;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.enums.DismissalType;
import com.cricarena.backend.entity.enums.ExtraType;
import com.cricarena.backend.entity.enums.InningsStatus;
import com.cricarena.backend.entity.enums.MatchResultType;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchDeliveryMapper;
import com.cricarena.backend.repository.MatchDeliveryRepository;
import com.cricarena.backend.repository.MatchInningsRepository;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.service.ScorecardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScorecardServiceImpl implements ScorecardService {

    private final MatchRepository matchRepository;
    private final MatchInningsRepository matchInningsRepository;
    private final MatchDeliveryRepository matchDeliveryRepository;
    private final MatchPlayingXIRepository matchPlayingXIRepository;
    private final MatchDeliveryMapper matchDeliveryMapper;

    public ScorecardServiceImpl(MatchRepository matchRepository,
                                 MatchInningsRepository matchInningsRepository,
                                 MatchDeliveryRepository matchDeliveryRepository,
                                 MatchPlayingXIRepository matchPlayingXIRepository,
                                 MatchDeliveryMapper matchDeliveryMapper) {
        this.matchRepository = matchRepository;
        this.matchInningsRepository = matchInningsRepository;
        this.matchDeliveryRepository = matchDeliveryRepository;
        this.matchPlayingXIRepository = matchPlayingXIRepository;
        this.matchDeliveryMapper = matchDeliveryMapper;
    }

    @Override
    @Transactional
    public ScorecardResponse getScorecard(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        List<MatchInnings> inningsList = matchInningsRepository
                .findAllByMatch_IdOrderByInningsNumberAsc(matchId);

        List<MatchPlayingXI> allMatchPlayingXI = matchPlayingXIRepository.findAllByMatch_Id(matchId);
        Map<Long, String> playerNameSnapshotMap = allMatchPlayingXI.stream()
                .filter(xi -> xi.getPlayer() != null && xi.getPlayerName() != null)
                .collect(Collectors.toMap(
                        xi -> xi.getPlayer().getId(),
                        MatchPlayingXI::getPlayerName,
                        (existing, replacement) -> existing
                ));

        // Build innings scorecards
        InningsScorecardResponse sc1 = null;
        InningsScorecardResponse sc2 = null;

        for (MatchInnings innings : inningsList) {
            List<MatchDelivery> deliveries = matchDeliveryRepository
                    .findAllByInnings_IdOrderByBallSequenceAsc(innings.getId());

            List<MatchPlayingXI> playingXI = matchPlayingXIRepository
                    .findAllByMatch_IdAndTeam_Id(matchId, innings.getBattingTeam().getId());

            InningsScorecardResponse sc = buildInningsScorecard(innings, deliveries, playingXI, playerNameSnapshotMap);

            if (innings.getInningsNumber() == 1) {
                sc1 = sc;
            } else {
                sc2 = sc;
            }
        }

        // Compute / retrieve match result
        MatchResultResponse result = null;

        if (match.getStatus() == MatchStatus.ABANDONED) {
            result = new MatchResultResponse();
            result.setResultType(MatchResultType.NO_RESULT);
            result.setResultSummary(match.getResultSummary() != null
                    ? match.getResultSummary()
                    : "Match abandoned / no result");
        } else if (match.getStatus() == MatchStatus.CANCELLED) {
            result = new MatchResultResponse();
            result.setResultType(MatchResultType.NO_RESULT);
            result.setResultSummary(match.getResultSummary() != null
                    ? match.getResultSummary()
                    : "Match cancelled / no result");
        } else if (match.getStatus() == MatchStatus.COMPLETED && match.getResultType() != null) {
            // Already persisted result
            result = buildResultFromMatch(match);
        } else if (sc1 != null && sc2 != null
                && sc1.getStatus() == InningsStatus.COMPLETED
                && sc2.getStatus() == InningsStatus.COMPLETED) {
            // Both innings done — compute & persist result
            result = computeAndPersistResult(match, inningsList);
        }

        ScorecardResponse response = new ScorecardResponse();
        response.setMatchId(matchId);
        response.setMatchStatus(match.getStatus());
        response.setInnings1(sc1);
        response.setInnings2(sc2);
        response.setResult(result);
        return response;
    }

    // -------------------------------------------------------------------------
    // Build InningsScorecardResponse
    // -------------------------------------------------------------------------

    private InningsScorecardResponse buildInningsScorecard(
            MatchInnings innings,
            List<MatchDelivery> deliveries,
            List<MatchPlayingXI> playingXI,
            Map<Long, String> nameMap) {

        InningsScorecardResponse sc = new InningsScorecardResponse();
        sc.setId(innings.getId());
        sc.setMatchId(innings.getMatch().getId());
        sc.setInningsNumber(innings.getInningsNumber());
        sc.setBattingTeamId(innings.getBattingTeam().getId());
        sc.setBattingTeamName(innings.getBattingTeam().getName());
        sc.setBowlingTeamId(innings.getBowlingTeam().getId());
        sc.setBowlingTeamName(innings.getBowlingTeam().getName());
        sc.setTotalRuns(innings.getTotalRuns());
        sc.setWickets(innings.getWickets());
        sc.setLegalBalls(innings.getLegalBalls());
        sc.setOvers(formatOvers(innings.getLegalBalls()));
        sc.setMaxOvers(innings.getMaxOvers());
        sc.setTargetRuns(innings.getTargetRuns());
        sc.setStatus(innings.getStatus());

        // Run rate
        double runRate = 0.0;
        if (innings.getLegalBalls() != null && innings.getLegalBalls() > 0) {
            runRate = round2((innings.getTotalRuns() * 6.0) / innings.getLegalBalls());
        }
        sc.setRunRate(runRate);

        // Batting scorecard
        sc.setBatters(buildBattingScorecard(deliveries, playingXI, nameMap));

        // Bowling scorecard
        sc.setBowlers(buildBowlingScorecard(deliveries, nameMap));

        // Extras
        sc.setExtras(buildExtras(deliveries));

        // Fall of wickets
        sc.setFallOfWickets(buildFallOfWickets(deliveries, nameMap));

        // Recent deliveries (last 12)
        List<DeliveryResponse> recentDeliveries = new ArrayList<>();
        int start = Math.max(0, deliveries.size() - 12);
        for (int i = deliveries.size() - 1; i >= start; i--) {
            recentDeliveries.add(matchDeliveryMapper.toResponse(deliveries.get(i)));
        }
        sc.setRecentDeliveries(recentDeliveries);

        return sc;
    }

    // -------------------------------------------------------------------------
    // Batting scorecard
    // -------------------------------------------------------------------------

    private List<BatterScorecardEntry> buildBattingScorecard(
            List<MatchDelivery> deliveries,
            List<MatchPlayingXI> playingXI,
            Map<Long, String> nameMap) {

        // Track each batter's stats. Keep insertion order (order of first appearance).
        LinkedHashMap<Long, BatterStats> statsMap = new LinkedHashMap<>();

        for (MatchDelivery d : deliveries) {
            Player striker = d.getStriker();
            statsMap.computeIfAbsent(striker.getId(), id -> new BatterStats(striker));

            BatterStats stats = statsMap.get(striker.getId());
            stats.runs += d.getBatterRuns();

            // Ball faced: everything except WIDE
            if (d.getExtraType() != ExtraType.WIDE) {
                stats.ballsFaced++;
            }

            // 4s and 6s: only off-bat (NONE extra type)
            if (d.getExtraType() == ExtraType.NONE) {
                if (d.getBatterRuns() == 4) stats.fours++;
                if (d.getBatterRuns() == 6) stats.sixes++;
            }
        }

        // Record dismissals — find the wicket delivery where this batter was dismissed
        for (MatchDelivery d : deliveries) {
            if (!d.isWicket() || d.getDismissedPlayer() == null) continue;
            Long dismissedId = d.getDismissedPlayer().getId();
            BatterStats stats = statsMap.get(dismissedId);
            if (stats == null) {
                // Dismissed player may not have faced a ball (e.g. run out from non-striker)
                // Create their entry now
                stats = new BatterStats(d.getDismissedPlayer());
                statsMap.put(dismissedId, stats);
            }
            stats.dismissed = true;
            stats.dismissalType = d.getDismissalType();
            stats.dismissalText = buildDismissalText(d, nameMap);
        }

        // Build ordered list from playing XI (insert DNBs at the end)
        Set<Long> appeared = new HashSet<>(statsMap.keySet());
        List<BatterScorecardEntry> result = new ArrayList<>();

        // Add appeared batters in order of appearance
        for (Map.Entry<Long, BatterStats> entry : statsMap.entrySet()) {
            result.add(toEntry(entry.getValue(), nameMap));
        }

        // Add did-not-bat from playing XI
        for (MatchPlayingXI xi : playingXI) {
            Long pid = xi.getPlayer().getId();
            if (!appeared.contains(pid)) {
                BatterScorecardEntry dnb = new BatterScorecardEntry();
                dnb.setPlayerId(pid);
                dnb.setPlayerName(xi.getPlayerName());
                dnb.setRuns(0);
                dnb.setBallsFaced(0);
                dnb.setFours(0);
                dnb.setSixes(0);
                dnb.setStrikeRate(0.0);
                dnb.setNotOut(false);
                dnb.setDidNotBat(true);
                result.add(dnb);
            }
        }

        return result;
    }

    private BatterScorecardEntry toEntry(BatterStats stats, Map<Long, String> nameMap) {
        BatterScorecardEntry e = new BatterScorecardEntry();
        e.setPlayerId(stats.player.getId());
        e.setPlayerName(resolvePlayerName(stats.player, nameMap));
        e.setRuns(stats.runs);
        e.setBallsFaced(stats.ballsFaced);
        e.setFours(stats.fours);
        e.setSixes(stats.sixes);
        double sr = stats.ballsFaced > 0 ? round2((stats.runs * 100.0) / stats.ballsFaced) : 0.0;
        e.setStrikeRate(sr);
        e.setNotOut(!stats.dismissed);
        e.setDidNotBat(false);
        if (stats.dismissed) {
            e.setDismissalType(stats.dismissalType);
            e.setDismissalText(stats.dismissalText);
        } else {
            e.setDismissalText("not out");
        }
        return e;
    }

    private String buildDismissalText(MatchDelivery d, Map<Long, String> nameMap) {
        if (d.getDismissalType() == null) return "";
        String bowlerName = d.getBowler() != null ? resolvePlayerName(d.getBowler(), nameMap) : "unknown";
        String fielderName = d.getFielder() != null ? resolvePlayerName(d.getFielder(), nameMap) : null;

        return switch (d.getDismissalType()) {
            case BOWLED -> "b " + bowlerName;
            case CAUGHT -> (fielderName != null && (d.getBowler() == null || !d.getFielder().getId().equals(d.getBowler().getId())))
                    ? "c " + fielderName + " b " + bowlerName
                    : "c & b " + bowlerName;
            case LBW -> "lbw b " + bowlerName;
            case STUMPED -> "st " + (fielderName != null ? fielderName : "wk") + " b " + bowlerName;
            case HIT_WICKET -> "hit wicket b " + bowlerName;
            case RUN_OUT -> fielderName != null
                    ? "run out (" + fielderName + ")"
                    : "run out";
            case OBSTRUCTING_FIELD -> "obstructing the field";
        };
    }

    // -------------------------------------------------------------------------
    // Bowling scorecard
    // -------------------------------------------------------------------------

    private List<BowlerScorecardEntry> buildBowlingScorecard(List<MatchDelivery> deliveries, Map<Long, String> nameMap) {
        // Group deliveries by bowler, maintaining first-appearance order
        LinkedHashMap<Long, BowlerStats> statsMap = new LinkedHashMap<>();

        for (MatchDelivery d : deliveries) {
            Player bowler = d.getBowler();
            statsMap.computeIfAbsent(bowler.getId(), id -> new BowlerStats(bowler));

            BowlerStats stats = statsMap.get(bowler.getId());

            // Runs against bowler: batter runs + extras for NONE/WIDE/NO_BALL; NOT for BYE/LEG_BYE
            ExtraType et = d.getExtraType();
            int bowlerRunsOnBall = (et == ExtraType.NONE || et == ExtraType.WIDE || et == ExtraType.NO_BALL)
                    ? d.getBatterRuns() + d.getExtraRuns()
                    : 0;
            stats.runsConceded += bowlerRunsOnBall;
            stats.bowlerRunsPerOver.merge(d.getOverNumber(), bowlerRunsOnBall, Integer::sum);

            // Legal ball count & per-over legal ball count (for maidens)
            if (d.isLegal()) {
                stats.legalBalls++;
                stats.legalBallsPerOver.merge(d.getOverNumber(), 1, Integer::sum);
            }

            // Wickets: credit bowler except RUN_OUT and OBSTRUCTING_FIELD
            if (d.isWicket() && d.getDismissalType() != null
                    && d.getDismissalType() != DismissalType.RUN_OUT
                    && d.getDismissalType() != DismissalType.OBSTRUCTING_FIELD) {
                stats.wickets++;
            }
        }

        // Count maidens: an over is maiden if it has 6 legal deliveries and 0 bowler runs across ALL balls in that over
        for (BowlerStats stats : statsMap.values()) {
            int maidens = 0;
            for (Map.Entry<Integer, Integer> entry : stats.legalBallsPerOver.entrySet()) {
                if (entry.getValue() == 6 && stats.bowlerRunsPerOver.getOrDefault(entry.getKey(), 0) == 0) {
                    maidens++;
                }
            }
            stats.maidens = maidens;
        }

        List<BowlerScorecardEntry> result = new ArrayList<>();
        for (BowlerStats stats : statsMap.values()) {
            BowlerScorecardEntry e = new BowlerScorecardEntry();
            e.setPlayerId(stats.player.getId());
            e.setPlayerName(resolvePlayerName(stats.player, nameMap));
            e.setOvers(formatOvers(stats.legalBalls));
            e.setMaidens(stats.maidens);
            e.setRunsConceded(stats.runsConceded);
            e.setWickets(stats.wickets);

            double economy = 0.0;
            if (stats.legalBalls > 0) {
                economy = round2((stats.runsConceded * 6.0) / stats.legalBalls);
            }
            e.setEconomyRate(economy);
            result.add(e);
        }
        return result;
    }

    private String resolvePlayerName(Player player, Map<Long, String> nameMap) {
        if (player == null) return "unknown";
        if (nameMap != null && nameMap.containsKey(player.getId())) {
            return nameMap.get(player.getId());
        }
        return player.getFullName();
    }

    // -------------------------------------------------------------------------
    // Extras
    // -------------------------------------------------------------------------

    private ExtrasResponse buildExtras(List<MatchDelivery> deliveries) {
        int wides = 0, noBalls = 0, byes = 0, legByes = 0;
        for (MatchDelivery d : deliveries) {
            switch (d.getExtraType()) {
                case WIDE -> wides += d.getExtraRuns();
                case NO_BALL -> noBalls += d.getExtraRuns();
                case BYE -> byes += d.getExtraRuns();
                case LEG_BYE -> legByes += d.getExtraRuns();
                default -> { /* NONE */ }
            }
        }
        return new ExtrasResponse(wides, noBalls, byes, legByes);
    }

    // -------------------------------------------------------------------------
    // Fall of wickets
    // -------------------------------------------------------------------------

    private List<FallOfWicketResponse> buildFallOfWickets(List<MatchDelivery> deliveries, Map<Long, String> nameMap) {
        List<FallOfWicketResponse> fow = new ArrayList<>();
        int runningTotal = 0;
        int wicketNum = 0;
        int legalBalls = 0;

        for (MatchDelivery d : deliveries) {
            runningTotal += d.getTotalRuns();
            if (d.isLegal()) legalBalls++;

            if (d.isWicket() && d.getDismissedPlayer() != null) {
                wicketNum++;
                FallOfWicketResponse f = new FallOfWicketResponse();
                f.setWicketNumber(wicketNum);
                f.setScore(runningTotal);
                f.setOvers(formatOvers(legalBalls));
                f.setDismissedPlayerId(d.getDismissedPlayer().getId());
                f.setDismissedPlayerName(resolvePlayerName(d.getDismissedPlayer(), nameMap));
                fow.add(f);
            }
        }
        return fow;
    }

    // -------------------------------------------------------------------------
    // Result calculation
    // -------------------------------------------------------------------------

    private MatchResultResponse computeAndPersistResult(
            Match match,
            List<MatchInnings> inningsList) {

        MatchInnings innings1 = inningsList.stream()
                .filter(i -> i.getInningsNumber() == 1).findFirst().orElse(null);
        MatchInnings innings2 = inningsList.stream()
                .filter(i -> i.getInningsNumber() == 2).findFirst().orElse(null);

        if (innings1 == null || innings2 == null) return null;

        MatchResultResponse result = applyMatchResult(match, innings1, innings2);
        matchRepository.save(match);
        return result;
    }

    /**
     * Computes match result from two completed innings and applies result fields and COMPLETED status to {@code match}.
     */
    public static MatchResultResponse applyMatchResult(Match match, MatchInnings innings1, MatchInnings innings2) {
        int runs1 = innings1.getTotalRuns();
        int runs2 = innings2.getTotalRuns();
        int wickets2 = innings2.getWickets();

        MatchResultType resultType;
        Integer margin;
        String summary;
        com.cricarena.backend.entity.Team winner = null;

        if (runs2 > runs1) {
            resultType = MatchResultType.WINNER_BY_WICKETS;
            margin = 10 - wickets2;
            winner = innings2.getBattingTeam();
            summary = winner.getName() + " won by " + margin + " wicket" + (margin == 1 ? "" : "s");
        } else if (runs1 > runs2) {
            resultType = MatchResultType.WINNER_BY_RUNS;
            margin = runs1 - runs2;
            winner = innings1.getBattingTeam();
            summary = winner.getName() + " won by " + margin + " run" + (margin == 1 ? "" : "s");
        } else {
            resultType = MatchResultType.TIE;
            margin = 0;
            summary = "Match Tied";
        }

        match.setResultType(resultType);
        match.setResultMargin(margin);
        match.setResultSummary(summary);
        match.setWinnerTeam(winner);
        match.setStatus(MatchStatus.COMPLETED);

        MatchResultResponse result = new MatchResultResponse();
        result.setResultType(resultType);
        result.setResultMargin(margin);
        result.setResultSummary(summary);
        if (winner != null) {
            result.setWinnerTeamId(winner.getId());
            result.setWinnerTeamName(winner.getName());
        }
        return result;
    }

    private MatchResultResponse buildResultFromMatch(Match match) {
        MatchResultResponse result = new MatchResultResponse();
        result.setResultType(match.getResultType());
        result.setResultMargin(match.getResultMargin());
        result.setResultSummary(match.getResultSummary());
        if (match.getWinnerTeam() != null) {
            result.setWinnerTeamId(match.getWinnerTeam().getId());
            result.setWinnerTeamName(match.getWinnerTeam().getName());
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String formatOvers(int legalBalls) {
        return (legalBalls / 6) + "." + (legalBalls % 6);
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // -------------------------------------------------------------------------
    // Inner stat holders
    // -------------------------------------------------------------------------

    private static class BatterStats {
        final Player player;
        int runs = 0;
        int ballsFaced = 0;
        int fours = 0;
        int sixes = 0;
        boolean dismissed = false;
        DismissalType dismissalType;
        String dismissalText;

        BatterStats(Player player) {
            this.player = player;
        }
    }

    private static class BowlerStats {
        final Player player;
        int legalBalls = 0;
        int runsConceded = 0;
        int wickets = 0;
        int maidens = 0;
        Map<Integer, Integer> legalBallsPerOver = new LinkedHashMap<>();
        Map<Integer, Integer> bowlerRunsPerOver = new LinkedHashMap<>();

        BowlerStats(Player player) {
            this.player = player;
        }
    }
}
