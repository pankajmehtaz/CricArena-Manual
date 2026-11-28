package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.*;
import com.cricarena.backend.entity.*;
import com.cricarena.backend.entity.enums.*;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchDeliveryMapper;
import com.cricarena.backend.repository.*;
import com.cricarena.backend.service.impl.ScorecardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScorecardServiceImplTest {

    @Mock private MatchRepository matchRepository;
    @Mock private MatchInningsRepository matchInningsRepository;
    @Mock private MatchDeliveryRepository matchDeliveryRepository;
    @Mock private MatchPlayingXIRepository matchPlayingXIRepository;
    @Spy  private MatchDeliveryMapper matchDeliveryMapper;

    @InjectMocks
    private ScorecardServiceImpl scorecardService;

    // ---- shared fixtures ----
    private College college;
    private Tournament tournament;
    private Team teamA, teamB;
    private Match match;
    private MatchInnings innings1, innings2;
    private Player batter1, batter2, bowler1, bowler2, keeper;

    @BeforeEach
    void setUp() {
        college = new College();
        college.setId(1L);

        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("T28E Cup");

        teamA = new Team();
        teamA.setId(1L);
        teamA.setName("Alpha XI");

        teamB = new Team();
        teamB.setId(2L);
        teamB.setName("Beta XI");

        match = new Match(tournament, teamA, teamB,
                LocalDateTime.now().plusDays(1), "Ground", MatchFormat.CUSTOM, 2, MatchStatus.IN_PROGRESS);
        match.setId(10L);

        innings1 = makeInnings(1L, match, 1, teamA, teamB, InningsStatus.IN_PROGRESS);
        innings2 = makeInnings(2L, match, 2, teamB, teamA, InningsStatus.IN_PROGRESS);

        batter1 = makePlayer(101L, "Batter One");
        batter2 = makePlayer(102L, "Batter Two");
        bowler1 = makePlayer(201L, "Bowler One");
        bowler2 = makePlayer(202L, "Bowler Two");
        keeper  = makePlayer(301L, "Keeper One");
    }

    // ==========================================================================
    // 1. 404 when match not found
    // ==========================================================================

    @Test
    @DisplayName("throws ResourceNotFoundException when match does not exist")
    void matchNotFound_throws() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> scorecardService.getScorecard(99L));
    }

    // ==========================================================================
    // 2. Live match with no innings → empty scorecard
    // ==========================================================================

    @Test
    @DisplayName("returns empty scorecard for a match with no innings yet")
    void noInnings_emptyScorecardReturned() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of());

        ScorecardResponse sc = scorecardService.getScorecard(10L);

        assertNotNull(sc);
        assertEquals(10L, sc.getMatchId());
        assertNull(sc.getInnings1());
        assertNull(sc.getInnings2());
        assertNull(sc.getResult());
    }

    // ==========================================================================
    // 3. Batter runs, balls faced, fours, sixes
    // ==========================================================================

    @Test
    @DisplayName("correctly calculates batter runs, balls faced, fours and sixes")
    void batterRunsFoursSixes() {
        innings1.setTotalRuns(16); innings1.setLegalBalls(4);

        // Ball 1: 4 runs (legal, NONE)
        MatchDelivery d1 = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                4, ExtraType.NONE, 0, 4, true);
        // Ball 2: 6 runs (legal, NONE)
        MatchDelivery d2 = makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2,
                6, ExtraType.NONE, 0, 6, true);
        // Ball 3: 1 run (legal, NONE)
        MatchDelivery d3 = makeDelivery(innings1, 3, 1, 3, bowler1, batter1, batter2,
                1, ExtraType.NONE, 0, 1, true);
        // Ball 4: wide 5 (not legal, batter1 didn't face)
        MatchDelivery d4 = makeDelivery(innings1, 4, 1, 4, bowler1, batter1, batter2,
                0, ExtraType.WIDE, 5, 5, false);

        stubInnings1WithDeliveries(List.of(d1, d2, d3, d4));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());

        assertNotNull(b1);
        assertEquals(11, b1.getRuns());           // 4+6+1
        assertEquals(3, b1.getBallsFaced());       // d1, d2, d3 (wide excluded)
        assertEquals(1, b1.getFours());
        assertEquals(1, b1.getSixes());
    }

    // ==========================================================================
    // 4. Strike rate calculation
    // ==========================================================================

    @Test
    @DisplayName("strike rate is runs * 100 / ballsFaced, rounded to 2dp")
    void strikeRateCalculation() {
        innings1.setTotalRuns(17); innings1.setLegalBalls(6);

        int[] runs = {4, 6, 4, 2, 1, 0};
        List<MatchDelivery> deliveries = new ArrayList<>();
        // batter1 scores 17 off 6 balls
        for (int i = 1; i <= 6; i++) {
            deliveries.add(makeDelivery(innings1, i, 0, i, bowler1, batter1, batter2,
                    runs[i - 1], ExtraType.NONE, 0, runs[i - 1], true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());

        assertNotNull(b1);
        assertEquals(17, b1.getRuns());
        assertEquals(6, b1.getBallsFaced());
        assertEquals(Math.round(17.0 * 100 / 6 * 100) / 100.0, b1.getStrikeRate());
    }

    // ==========================================================================
    // 5. Wide does not count as ball faced; no-ball counts as 1 ball faced
    // ==========================================================================

    @Test
    @DisplayName("wide is not a ball faced; no-ball counts as 1 ball faced")
    void wideAndNoBallBallsFaced() {
        innings1.setTotalRuns(3); innings1.setLegalBalls(1);

        // Wide: not counted for batter
        MatchDelivery wide = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                0, ExtraType.WIDE, 1, 1, false);
        // No-ball with 2 batter runs: counts as 1 ball faced
        MatchDelivery nb = makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2,
                2, ExtraType.NO_BALL, 1, 3, false);

        stubInnings1WithDeliveries(List.of(wide, nb));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());

        assertNotNull(b1);
        assertEquals(2, b1.getRuns());        // only batter runs off nb
        assertEquals(1, b1.getBallsFaced());  // wide=0, nb=1
    }

    // ==========================================================================
    // 6. Bye and leg-bye count as 1 ball faced each; runs go to extras not batter
    // ==========================================================================

    @Test
    @DisplayName("bye and leg-bye count as 1 ball faced but 0 batter runs")
    void byeAndLegByeBallsFaced() {
        innings1.setTotalRuns(4); innings1.setLegalBalls(2);

        MatchDelivery bye = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                0, ExtraType.BYE, 2, 2, true);
        MatchDelivery legBye = makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2,
                0, ExtraType.LEG_BYE, 2, 2, true);

        stubInnings1WithDeliveries(List.of(bye, legBye));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());

        assertNotNull(b1);
        assertEquals(0, b1.getRuns());
        assertEquals(2, b1.getBallsFaced());  // both bye and leg-bye count

        // Extras should capture byes and leg-byes
        ExtrasResponse extras = sc.getInnings1().getExtras();
        assertEquals(2, extras.getByes());
        assertEquals(2, extras.getLegByes());
        assertEquals(4, extras.getTotal());
    }

    // ==========================================================================
    // 7. Bowler runs and wickets (RUN_OUT not credited to bowler)
    // ==========================================================================

    @Test
    @DisplayName("bowler credited correctly; RUN_OUT not counted as bowler wicket")
    void bowlerRunsAndWickets() {
        innings1.setTotalRuns(14); innings1.setLegalBalls(5);

        // Ball 1: bowler1, 4 batter runs
        MatchDelivery d1 = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                4, ExtraType.NONE, 0, 4, true);
        // Ball 2: bowler1, wide 2 (counted against bowler)
        MatchDelivery d2 = makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2,
                0, ExtraType.WIDE, 2, 2, false);
        // Ball 3: bowler1, bye 2 (NOT counted against bowler)
        MatchDelivery d3 = makeDelivery(innings1, 3, 1, 3, bowler1, batter1, batter2,
                0, ExtraType.BYE, 2, 2, true);
        // Ball 4: bowler1, wicket BOWLED
        MatchDelivery d4 = makeDelivery(innings1, 4, 1, 4, bowler1, batter1, batter2,
                0, ExtraType.NONE, 0, 0, true);
        d4.setWicket(true); d4.setDismissalType(DismissalType.BOWLED); d4.setDismissedPlayer(batter1);
        // Ball 5: bowler1, wicket RUN_OUT (should NOT credit bowler)
        MatchDelivery d5 = makeDelivery(innings1, 5, 1, 5, bowler1, batter2, batter1,
                0, ExtraType.NONE, 0, 0, true);
        d5.setWicket(true); d5.setDismissalType(DismissalType.RUN_OUT); d5.setDismissedPlayer(batter2);

        stubInnings1WithDeliveries(List.of(d1, d2, d3, d4, d5));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BowlerScorecardEntry bwl = findBowler(sc.getInnings1(), bowler1.getId());

        assertNotNull(bwl);
        // runs: 4 (NONE, batter) + 2 (WIDE extra) + 0 (bye excluded) + 0 + 0 = 6
        assertEquals(6, bwl.getRunsConceded());
        // wickets: BOWLED=1, RUN_OUT not credited
        assertEquals(1, bwl.getWickets());
    }

    // ==========================================================================
    // 8. Maiden over
    // ==========================================================================

    @Test
    @DisplayName("a completed over with 0 bowler runs is a maiden")
    void maidenOverDetected() {
        innings1.setTotalRuns(2); innings1.setLegalBalls(6);

        List<MatchDelivery> deliveries = new ArrayList<>();
        // 6 legal deliveries in over 1, all dot balls (byes don't count vs bowler)
        for (int i = 1; i <= 6; i++) {
            ExtraType et = (i == 1) ? ExtraType.BYE : ExtraType.NONE;
            int extraRuns = (i == 1) ? 2 : 0;
            deliveries.add(makeDelivery(innings1, i, 1, i, bowler1, batter1, batter2,
                    0, et, extraRuns, extraRuns, true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BowlerScorecardEntry bwl = findBowler(sc.getInnings1(), bowler1.getId());

        assertNotNull(bwl);
        assertEquals(1, bwl.getMaidens());    // 0 bowler runs in a completed over
        assertEquals(0, bwl.getRunsConceded()); // bye not counted vs bowler
    }

    // ==========================================================================
    // 9. Economy rate
    // ==========================================================================

    @Test
    @DisplayName("economy rate = runsConceded / (legalBalls / 6.0)")
    void economyRate() {
        innings1.setTotalRuns(12); innings1.setLegalBalls(6);

        List<MatchDelivery> deliveries = new ArrayList<>();
        // 6 legal balls, 2 runs each = 12 runs against bowler
        for (int i = 1; i <= 6; i++) {
            deliveries.add(makeDelivery(innings1, i, 1, i, bowler1, batter1, batter2,
                    2, ExtraType.NONE, 0, 2, true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BowlerScorecardEntry bwl = findBowler(sc.getInnings1(), bowler1.getId());

        assertNotNull(bwl);
        assertEquals(12, bwl.getRunsConceded());
        assertEquals("1.0", bwl.getOvers());
        // economy = 12 / 1.0 = 12.0
        assertEquals(12.0, bwl.getEconomyRate());
    }

    // ==========================================================================
    // 10. Run rate
    // ==========================================================================

    @Test
    @DisplayName("innings run rate = totalRuns * 6 / legalBalls")
    void inningsRunRate() {
        innings1.setTotalRuns(18); innings1.setLegalBalls(6);

        List<MatchDelivery> deliveries = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            deliveries.add(makeDelivery(innings1, i, 1, i, bowler1, batter1, batter2,
                    3, ExtraType.NONE, 0, 3, true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        // runRate = 18 * 6 / 6 = 18.0
        assertEquals(18.0, sc.getInnings1().getRunRate());
    }

    // ==========================================================================
    // 11. Chase win — 2nd team wins by wickets
    // ==========================================================================

    @Test
    @DisplayName("chase win: 2nd batting team wins by (10 - wickets) wickets")
    void chaseWin_winnerByWickets() {
        match.setStatus(MatchStatus.IN_PROGRESS);
        innings1.setStatus(InningsStatus.COMPLETED);
        innings1.setTotalRuns(80); innings1.setLegalBalls(12); innings1.setWickets(10);

        innings2.setStatus(InningsStatus.COMPLETED);
        innings2.setTotalRuns(84); innings2.setLegalBalls(10); innings2.setWickets(3);

        stubBothInningsNoDeliveries();
        when(matchRepository.save(any())).thenReturn(match);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        MatchResultResponse result = sc.getResult();

        assertNotNull(result);
        assertEquals(MatchResultType.WINNER_BY_WICKETS, result.getResultType());
        assertEquals(teamB.getId(), result.getWinnerTeamId());
        assertEquals(7, result.getResultMargin()); // 10 - 3
        assertTrue(result.getResultSummary().contains("7 wickets"));
    }

    // ==========================================================================
    // 12. First-team defense win — wins by runs
    // ==========================================================================

    @Test
    @DisplayName("defense win: first batting team wins by runs")
    void defenseWin_winnerByRuns() {
        match.setStatus(MatchStatus.IN_PROGRESS);
        innings1.setStatus(InningsStatus.COMPLETED);
        innings1.setTotalRuns(100); innings1.setLegalBalls(12); innings1.setWickets(10);

        innings2.setStatus(InningsStatus.COMPLETED);
        innings2.setTotalRuns(80); innings2.setLegalBalls(12); innings2.setWickets(10);

        stubBothInningsNoDeliveries();
        when(matchRepository.save(any())).thenReturn(match);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        MatchResultResponse result = sc.getResult();

        assertNotNull(result);
        assertEquals(MatchResultType.WINNER_BY_RUNS, result.getResultType());
        assertEquals(teamA.getId(), result.getWinnerTeamId());
        assertEquals(20, result.getResultMargin());
        assertTrue(result.getResultSummary().contains("20 runs"));
    }

    // ==========================================================================
    // 13. Tie result
    // ==========================================================================

    @Test
    @DisplayName("tie: both innings end with equal scores → TIE result")
    void tieResult() {
        match.setStatus(MatchStatus.IN_PROGRESS);
        innings1.setStatus(InningsStatus.COMPLETED);
        innings1.setTotalRuns(75); innings1.setLegalBalls(12); innings1.setWickets(10);

        innings2.setStatus(InningsStatus.COMPLETED);
        innings2.setTotalRuns(75); innings2.setLegalBalls(12); innings2.setWickets(10);

        stubBothInningsNoDeliveries();
        when(matchRepository.save(any())).thenReturn(match);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        MatchResultResponse result = sc.getResult();

        assertNotNull(result);
        assertEquals(MatchResultType.TIE, result.getResultType());
        assertNull(result.getWinnerTeamId());
        assertEquals("Match Tied", result.getResultSummary());
    }

    // ==========================================================================
    // 14. Cancelled / no-result match
    // ==========================================================================

    @Test
    @DisplayName("cancelled match returns NO_RESULT")
    void cancelledMatch_noResult() {
        match.setStatus(MatchStatus.CANCELLED);

        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of());

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        MatchResultResponse result = sc.getResult();

        assertNotNull(result);
        assertEquals(MatchResultType.NO_RESULT, result.getResultType());
    }

    @Test
    @DisplayName("abandoned match returns NO_RESULT without inventing winner even after innings 1")
    void abandonedMatch_noResult() {
        match.setStatus(MatchStatus.ABANDONED);
        innings1.setStatus(InningsStatus.COMPLETED);
        innings1.setTotalRuns(90);

        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of(innings1));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings1.getId())).thenReturn(List.of());
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamA.getId())).thenReturn(List.of());

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        MatchResultResponse result = sc.getResult();

        assertNotNull(result);
        assertEquals(MatchResultType.NO_RESULT, result.getResultType());
        assertNull(result.getWinnerTeamId());
        assertEquals("Match abandoned / no result", result.getResultSummary());
    }

    @Test
    @DisplayName("over with 6 legal dot balls plus a wide is NOT a maiden over")
    void overWithWide_notMaiden() {
        innings1.setTotalRuns(1); innings1.setLegalBalls(6);

        List<MatchDelivery> deliveries = new ArrayList<>();
        deliveries.add(makeDelivery(innings1, 1, 0, 1, bowler1, batter1, batter2,
                0, ExtraType.WIDE, 1, 1, false));
        for (int i = 1; i <= 6; i++) {
            deliveries.add(makeDelivery(innings1, i + 1, 0, i, bowler1, batter1, batter2,
                    0, ExtraType.NONE, 0, 0, true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BowlerScorecardEntry bwl = findBowler(sc.getInnings1(), bowler1.getId());

        assertNotNull(bwl);
        assertEquals("1.0", bwl.getOvers());
        assertEquals(1, bwl.getRunsConceded());
        assertEquals(0, bwl.getMaidens());
    }

    @Test
    @DisplayName("did-not-bat players from Playing XI appear with didNotBat=true and caught-and-bowled is formatted")
    void didNotBatAndCaughtAndBowled() {
        innings1.setTotalRuns(0); innings1.setLegalBalls(1);

        MatchDelivery d = makeDelivery(innings1, 1, 0, 1, bowler1, batter1, batter2,
                0, ExtraType.NONE, 0, 0, true);
        d.setWicket(true);
        d.setDismissalType(DismissalType.CAUGHT);
        d.setDismissedPlayer(batter1);
        d.setFielder(bowler1);

        MatchPlayingXI xi1 = new MatchPlayingXI(match, teamA, batter1, batter1.getFullName(), 1, PlayingRole.BATSMAN, true, false);
        MatchPlayingXI xi2 = new MatchPlayingXI(match, teamA, batter2, batter2.getFullName(), 2, PlayingRole.BATSMAN, false, false);
        MatchPlayingXI xi3 = new MatchPlayingXI(match, teamA, keeper, keeper.getFullName(), 3, PlayingRole.WICKET_KEEPER, false, true);

        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of(innings1));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings1.getId())).thenReturn(List.of(d));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamA.getId())).thenReturn(List.of(xi1, xi2, xi3));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());
        BatterScorecardEntry k = findBatter(sc.getInnings1(), keeper.getId());

        assertNotNull(b1);
        assertEquals("c & b Bowler One", b1.getDismissalText());
        assertFalse(b1.isDidNotBat());

        assertNotNull(k);
        assertTrue(k.isDidNotBat());
        assertFalse(k.isNotOut());
    }

    // ==========================================================================
    // 15. Live match — only 1 innings in progress, no result yet
    // ==========================================================================

    @Test
    @DisplayName("live match with 1 innings in progress returns no result")
    void liveMatch_oneInningsInProgress_noResult() {
        innings1.setTotalRuns(30); innings1.setLegalBalls(6);

        List<MatchDelivery> deliveries = List.of(
                makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                        5, ExtraType.NONE, 0, 5, true)
        );

        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L))
                .thenReturn(List.of(innings1));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings1.getId()))
                .thenReturn(deliveries);
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamA.getId()))
                .thenReturn(List.of());

        ScorecardResponse sc = scorecardService.getScorecard(10L);

        assertNotNull(sc.getInnings1());
        assertNull(sc.getInnings2());
        assertNull(sc.getResult());
    }

    // ==========================================================================
    // 16. Extras breakdown
    // ==========================================================================

    @Test
    @DisplayName("extras correctly split into wides, no-balls, byes, leg-byes")
    void extrasBreakdown() {
        innings1.setTotalRuns(14); innings1.setLegalBalls(1);

        List<MatchDelivery> deliveries = List.of(
                makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2, 0, ExtraType.WIDE,   3, 3, false),
                makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2, 0, ExtraType.NO_BALL, 2, 2, false),
                makeDelivery(innings1, 3, 1, 3, bowler1, batter1, batter2, 0, ExtraType.BYE,     4, 4, true),
                makeDelivery(innings1, 4, 1, 4, bowler1, batter1, batter2, 0, ExtraType.LEG_BYE, 5, 5, true)
        );

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        ExtrasResponse extras = sc.getInnings1().getExtras();

        assertEquals(3,  extras.getWides());
        assertEquals(2,  extras.getNoBalls());
        assertEquals(4,  extras.getByes());
        assertEquals(5,  extras.getLegByes());
        assertEquals(14, extras.getTotal());
    }

    // ==========================================================================
    // 17. Fall of wickets
    // ==========================================================================

    @Test
    @DisplayName("fall of wickets listed in order with correct score and overs")
    void fallOfWickets() {
        innings1.setTotalRuns(20); innings1.setLegalBalls(4);

        MatchDelivery d1 = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                10, ExtraType.NONE, 0, 10, true);
        MatchDelivery d2 = makeDelivery(innings1, 2, 1, 2, bowler1, batter1, batter2,
                10, ExtraType.NONE, 0, 10, true);
        d2.setWicket(true);
        d2.setDismissalType(DismissalType.BOWLED);
        d2.setDismissedPlayer(batter1);

        MatchDelivery d3 = makeDelivery(innings1, 3, 1, 3, bowler1, batter2, batter1,
                0, ExtraType.NONE, 0, 0, true);
        MatchDelivery d4 = makeDelivery(innings1, 4, 1, 4, bowler1, batter2, batter1,
                0, ExtraType.NONE, 0, 0, true);
        d4.setWicket(true);
        d4.setDismissalType(DismissalType.LBW);
        d4.setDismissedPlayer(batter2);

        stubInnings1WithDeliveries(List.of(d1, d2, d3, d4));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        List<FallOfWicketResponse> fow = sc.getInnings1().getFallOfWickets();

        assertEquals(2, fow.size());
        assertEquals(1, fow.get(0).getWicketNumber());
        assertEquals(20, fow.get(0).getScore());
        assertEquals("0.2", fow.get(0).getOvers());

        assertEquals(2, fow.get(1).getWicketNumber());
        assertEquals(20, fow.get(1).getScore());
        assertEquals("0.4", fow.get(1).getOvers());
    }

    // ==========================================================================
    // 18. Dismissal text format
    // ==========================================================================

    @Test
    @DisplayName("dismissal text is correctly formatted for CAUGHT, BOWLED, RUN_OUT")
    void dismissalTextFormats() {
        innings1.setTotalRuns(0); innings1.setLegalBalls(3);

        MatchDelivery dBowled = makeDelivery(innings1, 1, 1, 1, bowler1, batter1, batter2,
                0, ExtraType.NONE, 0, 0, true);
        dBowled.setWicket(true);
        dBowled.setDismissalType(DismissalType.BOWLED);
        dBowled.setDismissedPlayer(batter1);

        Player keeper2 = makePlayer(302L, "Keeper Two");
        MatchDelivery dCaught = makeDelivery(innings1, 2, 1, 2, bowler1, batter2, keeper,
                0, ExtraType.NONE, 0, 0, true);
        dCaught.setWicket(true);
        dCaught.setDismissalType(DismissalType.CAUGHT);
        dCaught.setDismissedPlayer(batter2);
        dCaught.setFielder(keeper2);

        MatchDelivery dRunOut = makeDelivery(innings1, 3, 1, 3, bowler1, keeper, batter1,
                0, ExtraType.NONE, 0, 0, true);
        dRunOut.setWicket(true);
        dRunOut.setDismissalType(DismissalType.RUN_OUT);
        dRunOut.setDismissedPlayer(keeper);
        dRunOut.setFielder(keeper2);

        stubInnings1WithDeliveries(List.of(dBowled, dCaught, dRunOut));

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        List<BatterScorecardEntry> batters = sc.getInnings1().getBatters();

        BatterScorecardEntry b1 = findBatter(sc.getInnings1(), batter1.getId());
        BatterScorecardEntry b2 = findBatter(sc.getInnings1(), batter2.getId());
        BatterScorecardEntry k  = findBatter(sc.getInnings1(), keeper.getId());

        assertNotNull(b1);
        assertTrue(b1.getDismissalText().startsWith("b "));

        assertNotNull(b2);
        assertTrue(b2.getDismissalText().startsWith("c "));
        assertTrue(b2.getDismissalText().contains(" b "));

        assertNotNull(k);
        assertTrue(k.getDismissalText().startsWith("run out"));
    }

    // ==========================================================================
    // 19. Already-completed match reads persisted result (no recompute)
    // ==========================================================================

    @Test
    @DisplayName("completed match reads result from persisted entity without recomputing")
    void completedMatch_readsPersistedResult() {
        match.setStatus(MatchStatus.COMPLETED);
        match.setResultType(MatchResultType.WINNER_BY_RUNS);
        match.setResultMargin(15);
        match.setResultSummary("Alpha XI won by 15 runs");
        match.setWinnerTeam(teamA);

        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of());

        ScorecardResponse sc = scorecardService.getScorecard(10L);

        assertEquals(MatchResultType.WINNER_BY_RUNS, sc.getResult().getResultType());
        assertEquals(teamA.getId(), sc.getResult().getWinnerTeamId());
        assertEquals(15, sc.getResult().getResultMargin());
    }

    // ==========================================================================
    // 20. Recent deliveries limited to 12
    // ==========================================================================

    @Test
    @DisplayName("recent deliveries list contains at most 12 entries in reverse order")
    void recentDeliveriesMax12() {
        innings1.setTotalRuns(15); innings1.setLegalBalls(15);

        List<MatchDelivery> deliveries = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            int over = (i - 1) / 6 + 1;
            int ball = ((i - 1) % 6) + 1;
            deliveries.add(makeDelivery(innings1, i, over, ball, bowler1, batter1, batter2,
                    1, ExtraType.NONE, 0, 1, true));
        }

        stubInnings1WithDeliveries(deliveries);

        ScorecardResponse sc = scorecardService.getScorecard(10L);
        List<DeliveryResponse> recent = sc.getInnings1().getRecentDeliveries();

        assertEquals(12, recent.size());
    }

    // ==========================================================================
    // Helpers
    // ==========================================================================

    private void stubInnings1WithDeliveries(List<MatchDelivery> deliveries) {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L))
                .thenReturn(List.of(innings1));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings1.getId()))
                .thenReturn(deliveries);
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamA.getId()))
                .thenReturn(List.of());
    }

    private void stubBothInningsNoDeliveries() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L))
                .thenReturn(List.of(innings1, innings2));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings1.getId()))
                .thenReturn(List.of());
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(innings2.getId()))
                .thenReturn(List.of());
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamA.getId()))
                .thenReturn(List.of());
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, teamB.getId()))
                .thenReturn(List.of());
    }

    private BatterScorecardEntry findBatter(InningsScorecardResponse sc, Long playerId) {
        if (sc == null || sc.getBatters() == null) return null;
        return sc.getBatters().stream().filter(b -> playerId.equals(b.getPlayerId())).findFirst().orElse(null);
    }

    private BowlerScorecardEntry findBowler(InningsScorecardResponse sc, Long playerId) {
        if (sc == null || sc.getBowlers() == null) return null;
        return sc.getBowlers().stream().filter(b -> playerId.equals(b.getPlayerId())).findFirst().orElse(null);
    }

    @Test
    @DisplayName("Scorecard snapshot: resolves player names from MatchPlayingXI snapshot rather than current live Player entity")
    void getScorecard_usesMatchPlayingXISnapshot_preservesHistoricalNames() {
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findAllByMatch_IdOrderByInningsNumberAsc(10L)).thenReturn(List.of(innings1));

        // Delivery played by batter1 (current live name: "Batter One") and bowled by bowler1 ("Bowler One")
        MatchDelivery d = makeDelivery(innings1, 1, 0, 1, bowler1, batter1, batter2, 4, ExtraType.NONE, 0, 4, true);
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(1L)).thenReturn(List.of(d));

        // In MatchPlayingXI, snapshot names were "Historical Legend Batter" and "Historical Master Bowler"
        MatchPlayingXI xiBatter = new MatchPlayingXI(match, teamA, batter1, "Historical Legend Batter", 18, PlayingRole.BATSMAN, false, false);
        MatchPlayingXI xiBowler = new MatchPlayingXI(match, teamB, bowler1, "Historical Master Bowler", 99, PlayingRole.BOWLER, false, false);

        when(matchPlayingXIRepository.findAllByMatch_Id(10L)).thenReturn(List.of(xiBatter, xiBowler));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(10L, 1L)).thenReturn(List.of(xiBatter));

        // Change live player names to simulate future rename
        batter1.setFullName("Modern Renamed Batter");
        bowler1.setFullName("Modern Renamed Bowler");

        ScorecardResponse sc = scorecardService.getScorecard(10L);

        assertNotNull(sc.getInnings1());
        BatterScorecardEntry batterEntry = findBatter(sc.getInnings1(), 101L);
        assertNotNull(batterEntry);
        assertEquals("Historical Legend Batter", batterEntry.getPlayerName(), "Batter name should be snapshot name, not modern live name");

        BowlerScorecardEntry bowlerEntry = findBowler(sc.getInnings1(), 201L);
        assertNotNull(bowlerEntry);
        assertEquals("Historical Master Bowler", bowlerEntry.getPlayerName(), "Bowler name should be snapshot name, not modern live name");
    }

    // ---- entity factories ----

    private Player makePlayer(Long id, String fullName) {
        Player p = new Player();
        p.setId(id);
        p.setFullName(fullName);
        p.setPlayingRole(PlayingRole.BATSMAN);
        p.setBattingStyle(BattingStyle.RIGHT_HAND);
        return p;
    }

    private MatchInnings makeInnings(Long id, Match match, int number, Team batting, Team bowling, InningsStatus status) {
        MatchInnings innings = new MatchInnings(match, number, batting, bowling, 2, null, null, null);
        innings.setId(id);
        innings.setStatus(status);
        innings.setTotalRuns(0);
        innings.setWickets(0);
        innings.setLegalBalls(0);
        return innings;
    }

    private MatchDelivery makeDelivery(MatchInnings innings, int seq, int over, int ball,
                                       Player bowler, Player striker, Player nonStriker,
                                       int batterRuns, ExtraType extraType, int extraRuns,
                                       int totalRuns, boolean isLegal) {
        MatchDelivery d = new MatchDelivery(innings, seq, over, ball, bowler, striker, nonStriker,
                batterRuns, extraType, extraRuns, totalRuns, isLegal);
        d.setId((long) seq);
        return d;
    }
}
