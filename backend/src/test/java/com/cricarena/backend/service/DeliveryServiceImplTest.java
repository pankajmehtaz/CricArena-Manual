package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.ChangeBowlerRequest;
import com.cricarena.backend.dto.request.RecordDeliveryRequest;
import com.cricarena.backend.dto.response.DeliveryResultResponse;
import com.cricarena.backend.dto.response.FallOfWicketResponse;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchDelivery;
import com.cricarena.backend.entity.MatchInnings;
import com.cricarena.backend.entity.MatchPlayingXI;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.BowlingStyle;
import com.cricarena.backend.entity.enums.DismissalType;
import com.cricarena.backend.entity.enums.ExtraType;
import com.cricarena.backend.entity.enums.InningsStatus;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.mapper.InningsMapper;
import com.cricarena.backend.mapper.MatchDeliveryMapper;
import com.cricarena.backend.repository.MatchDeliveryRepository;
import com.cricarena.backend.repository.MatchInningsRepository;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.service.impl.DeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchInningsRepository matchInningsRepository;

    @Mock
    private MatchDeliveryRepository matchDeliveryRepository;

    @Mock
    private MatchPlayingXIRepository matchPlayingXIRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Spy
    private MatchDeliveryMapper matchDeliveryMapper;

    @Spy
    private InningsMapper inningsMapper;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private Match match;
    private MatchInnings innings;
    private Team teamA;
    private Team teamB;
    private Player striker;
    private Player nonStriker;
    private Player nextBatter;
    private Player bowler1;
    private Player bowler2;
    private Player fielder1;

    @BeforeEach
    void setUp() {
        Tournament tournament = new Tournament("Cup", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));
        tournament.setId(10L);

        College college = new College();
        college.setId(1L);

        teamA = new Team("Team A", "TA", "Ground A", "City", "ta@test.com", true, college);
        teamA.setId(100L);

        teamB = new Team("Team B", "TB", "Ground B", "City", "tb@test.com", true, college);
        teamB.setId(200L);

        match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0),
                "Ground", MatchFormat.T20, 20, MatchStatus.IN_PROGRESS);
        match.setId(1L);

        striker = new Player("Striker", 1, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "s@test.com", "123", null, true, teamA);
        striker.setId(11L);

        nonStriker = new Player("Non-Striker", 2, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "ns@test.com", "123", null, true, teamA);
        nonStriker.setId(12L);

        nextBatter = new Player("Next Batter", 5, PlayingRole.BATSMAN, BattingStyle.LEFT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "nb@test.com", "123", null, true, teamA);
        nextBatter.setId(13L);

        bowler1 = new Player("Bowler 1", 3, PlayingRole.BOWLER, BattingStyle.RIGHT_HAND,
                BowlingStyle.RIGHT_ARM_FAST, LocalDate.of(2000, 1, 1), "b1@test.com", "123", null, true, teamB);
        bowler1.setId(21L);

        bowler2 = new Player("Bowler 2", 4, PlayingRole.BOWLER, BattingStyle.RIGHT_HAND,
                BowlingStyle.RIGHT_ARM_SPIN, LocalDate.of(2000, 1, 1), "b2@test.com", "123", null, true, teamB);
        bowler2.setId(22L);

        fielder1 = new Player("Fielder 1", 6, PlayingRole.ALL_ROUNDER, BattingStyle.RIGHT_HAND,
                BowlingStyle.RIGHT_ARM_FAST, LocalDate.of(2000, 1, 1), "f1@test.com", "123", null, true, teamB);
        fielder1.setId(23L);

        innings = new MatchInnings(match, 1, teamA, teamB, 20, striker, nonStriker, bowler1);
        innings.setId(50L);
        innings.setStatus(InningsStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("recordDelivery - normal legal ball (0 runs) increments legal ball and maintains strike")
    void recordDelivery_dotBall_success() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> {
            MatchDelivery d = i.getArgument(0);
            d.setId(501L);
            return d;
        });
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getDelivery().getBallSequence());
        assertEquals(0, result.getDelivery().getOverNumber());
        assertEquals(1, result.getDelivery().getBallInOver());
        assertEquals(0, result.getDelivery().getTotalRuns());
        assertTrue(result.getDelivery().isLegal());

        assertEquals(0, result.getInnings().getTotalRuns());
        assertEquals(1, result.getInnings().getLegalBalls());
        assertEquals(11L, result.getInnings().getCurrentStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - odd runs rotate strike")
    void recordDelivery_oddRuns_rotatesStrike() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(1, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(1, result.getInnings().getTotalRuns());
        assertEquals(1, result.getInnings().getLegalBalls());
        assertEquals(12L, result.getInnings().getCurrentStrikerId());
        assertEquals(11L, result.getInnings().getCurrentNonStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - even runs do not rotate strike")
    void recordDelivery_evenRuns_noRotation() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(4, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(4, result.getInnings().getTotalRuns());
        assertEquals(1, result.getInnings().getLegalBalls());
        assertEquals(11L, result.getInnings().getCurrentStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - wide delivery is illegal, adds extra runs, and does not increment legal balls")
    void recordDelivery_wide_illegal() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.WIDE, 1);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertFalse(result.getDelivery().isLegal());
        assertEquals(1, result.getDelivery().getTotalRuns());
        assertEquals(1, result.getInnings().getTotalRuns());
        assertEquals(0, result.getInnings().getLegalBalls());
        assertEquals(11L, result.getInnings().getCurrentStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - no ball with boundary calculates total runs and remains illegal")
    void recordDelivery_noBallWithBoundary() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(4, ExtraType.NO_BALL, 1);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertFalse(result.getDelivery().isLegal());
        assertEquals(5, result.getDelivery().getTotalRuns());
        assertEquals(5, result.getInnings().getTotalRuns());
        assertEquals(0, result.getInnings().getLegalBalls());
        assertEquals(11L, result.getInnings().getCurrentStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - 6th legal ball rotates strike at over completion")
    void recordDelivery_overCompletion_rotatesEnds() {
        innings.setLegalBalls(5);
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(5L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(6, result.getInnings().getLegalBalls());
        assertEquals("1.0", result.getInnings().getOvers());
        assertEquals(12L, result.getInnings().getCurrentStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - odd runs on 6th legal ball does double rotation (net 0)")
    void recordDelivery_oddRunsOn6thBall_doubleRotation() {
        innings.setLegalBalls(5);
        RecordDeliveryRequest request = new RecordDeliveryRequest(1, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(5L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(6, result.getInnings().getLegalBalls());
        assertEquals(11L, result.getInnings().getCurrentStrikerId());
    }

    // =========================================================================
    // TASK 28D: WICKET MANAGEMENT TESTS
    // =========================================================================

    @Test
    @DisplayName("recordDelivery - Bowled dismissal increments wickets and replaces striker with nextBatter")
    void recordDelivery_bowled_strikerReplaced() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.BOWLED, 11L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(
                        new MatchPlayingXI(match, teamA, striker, "Striker", 1, PlayingRole.BATSMAN, false, false),
                        new MatchPlayingXI(match, teamA, nonStriker, "Non-Striker", 2, PlayingRole.BATSMAN, false, false),
                        new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)
                ));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> {
            MatchDelivery d = i.getArgument(0);
            d.setId(601L);
            return d;
        });
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertNotNull(result);
        assertTrue(result.getDelivery().isWicket());
        assertEquals(DismissalType.BOWLED, result.getDelivery().getDismissalType());
        assertEquals(11L, result.getDelivery().getDismissedPlayerId());
        assertEquals(13L, result.getDelivery().getNextBatterId());

        assertEquals(1, result.getInnings().getWickets());
        assertEquals(13L, result.getInnings().getCurrentStrikerId()); // Striker replaced by nextBatter
        assertEquals(12L, result.getInnings().getCurrentNonStrikerId()); // Non-striker untouched
    }

    @Test
    @DisplayName("recordDelivery - Caught dismissal requires valid fielder in bowling XI")
    void recordDelivery_caught_validFielder() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.CAUGHT, 11L, 23L, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(23L)).thenReturn(Optional.of(fielder1));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamB, fielder1, "Fielder 1", 6, PlayingRole.ALL_ROUNDER, false, false)));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));

        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertTrue(result.getDelivery().isWicket());
        assertEquals(DismissalType.CAUGHT, result.getDelivery().getDismissalType());
        assertEquals(23L, result.getDelivery().getFielderId());
        assertEquals(1, result.getInnings().getWickets());
    }

    @Test
    @DisplayName("recordDelivery - Caught dismissal missing fielder is rejected")
    void recordDelivery_caught_missingFielder_rejected() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.CAUGHT, 11L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("Fielder is required for dismissal type CAUGHT"));
    }

    @Test
    @DisplayName("recordDelivery - Caught dismissal with fielder not in bowling XI is rejected")
    void recordDelivery_caught_fielderNotInBowlingXI_rejected() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.CAUGHT, 11L, 23L, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(23L)).thenReturn(Optional.of(fielder1));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        // Bowling XI returns empty list (fielder not in XI)
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("is not in the bowling team's Playing XI"));
    }

    @Test
    @DisplayName("recordDelivery - Run out of non-striker on 1 run replaces non-striker with nextBatter")
    void recordDelivery_runOut_nonStriker() {
        // Striker runs 1, non-striker is run out
        RecordDeliveryRequest request = new RecordDeliveryRequest(1, ExtraType.NONE, 0,
                true, DismissalType.RUN_OUT, 12L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(12L)).thenReturn(Optional.of(nonStriker));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 12L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));

        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(1, result.getInnings().getTotalRuns());
        assertEquals(1, result.getInnings().getWickets());
        // Odd runs rotated ends: striker was rotated to non-striker position, but dismissed nonStriker (12) is replaced by 13
        assertEquals(13L, result.getInnings().getCurrentStrikerId());
        assertEquals(11L, result.getInnings().getCurrentNonStrikerId());
    }

    @Test
    @DisplayName("recordDelivery - Stumped off Wide is valid, does not increment legal balls, increments wickets")
    void recordDelivery_stumped_offWide() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.WIDE, 1,
                true, DismissalType.STUMPED, 11L, 23L, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(23L)).thenReturn(Optional.of(fielder1));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamB, fielder1, "Fielder 1", 6, PlayingRole.ALL_ROUNDER, false, false)));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));

        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertFalse(result.getDelivery().isLegal());
        assertTrue(result.getDelivery().isWicket());
        assertEquals(DismissalType.STUMPED, result.getDelivery().getDismissalType());
        assertEquals(1, result.getInnings().getTotalRuns()); // 1 wide extra
        assertEquals(0, result.getInnings().getLegalBalls()); // wide is illegal
        assertEquals(1, result.getInnings().getWickets());
    }

    @Test
    @DisplayName("recordDelivery - Bowled off Wide is rejected by Law 22")
    void recordDelivery_bowled_offWide_rejected() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.WIDE, 1,
                true, DismissalType.BOWLED, 11L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("Batter cannot be BOWLED off a WIDE"));
    }

    @Test
    @DisplayName("recordDelivery - Caught off No-Ball is rejected by Law 21")
    void recordDelivery_caught_offNoBall_rejected() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NO_BALL, 1,
                true, DismissalType.CAUGHT, 11L, 23L, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("Batter cannot be CAUGHT off a NO_BALL"));
    }

    @Test
    @DisplayName("recordDelivery - Run Out off No-Ball is valid")
    void recordDelivery_runOut_offNoBall_valid() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NO_BALL, 1,
                true, DismissalType.RUN_OUT, 11L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));

        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(0L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertTrue(result.getDelivery().isWicket());
        assertEquals(DismissalType.RUN_OUT, result.getDelivery().getDismissalType());
        assertEquals(1, result.getInnings().getTotalRuns());
        assertEquals(1, result.getInnings().getWickets());
    }

    @Test
    @DisplayName("recordDelivery - Dismissed player not currently at crease is rejected")
    void recordDelivery_dismissedPlayerNotAtCrease_rejected() {
        // Player 13 is in team but not at the crease
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.BOWLED, 13L, null, 11L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("Dismissed player must be one of the current batters at the crease"));
    }

    @Test
    @DisplayName("recordDelivery - Next batter already dismissed in this innings is rejected")
    void recordDelivery_nextBatterAlreadyDismissed_rejected() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.BOWLED, 11L, null, 13L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));
        // Next batter already dismissed previously
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("has already been dismissed in this innings"));
    }

    @Test
    @DisplayName("recordDelivery - 10th wicket triggers ALL-OUT, completes innings, and sets target for innings 1")
    void recordDelivery_10thWicket_allOut() {
        innings.setWickets(9); // 9 wickets down
        innings.setTotalRuns(150);

        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.BOWLED, 11L, null, null); // No next batter needed for 10th wicket

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(40L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(10, result.getInnings().getWickets());
        assertEquals(InningsStatus.COMPLETED, result.getInnings().getStatus());
        assertNull(result.getInnings().getCurrentStrikerId());
        assertNull(result.getInnings().getCurrentNonStrikerId());
        assertNull(result.getInnings().getCurrentBowlerId());
        assertEquals(151, result.getInnings().getTargetRuns()); // 150 + 1 = 151
    }

    @Test
    @DisplayName("recordDelivery - reaching max overs completes innings and sets target for innings 1")
    void recordDelivery_maxOvers_completesInnings() {
        innings.setLegalBalls(119); // 19.5 overs completed
        innings.setTotalRuns(180);

        RecordDeliveryRequest request = new RecordDeliveryRequest(1, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(119L);

        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(120, result.getInnings().getLegalBalls());
        assertEquals(181, result.getInnings().getTotalRuns());
        assertEquals(InningsStatus.COMPLETED, result.getInnings().getStatus());
        assertNull(result.getInnings().getCurrentStrikerId());
        assertNull(result.getInnings().getCurrentNonStrikerId());
        assertEquals(182, result.getInnings().getTargetRuns()); // 181 + 1 = 182
    }

    @Test
    @DisplayName("recordDelivery - rejected when innings is already COMPLETED")
    void recordDelivery_inningsCompleted_rejected() {
        innings.setStatus(InningsStatus.COMPLETED);
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> deliveryService.recordDelivery(1L, request));
        assertTrue(ex.getMessage().contains("Deliveries can only be recorded for an IN_PROGRESS innings"));
    }

    // =========================================================================
    // TASK 28C EXISTING BOWLER CHANGE TESTS
    // =========================================================================

    @Test
    @DisplayName("changeBowler - successfully changes bowler at over completion")
    void changeBowler_successAtOverEnd() {
        innings.setLegalBalls(6);
        ChangeBowlerRequest request = new ChangeBowlerRequest(22L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamB, bowler2, "Bowler 2", 4, PlayingRole.BOWLER, false, false)));
        when(playerRepository.findById(22L)).thenReturn(Optional.of(bowler2));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));

        InningsResponse response = deliveryService.changeBowler(1L, request);

        assertNotNull(response);
        assertEquals(22L, response.getCurrentBowlerId());
    }

    @Test
    @DisplayName("changeBowler - rejects bowler change in the middle of an over")
    void changeBowler_midOver_rejected() {
        innings.setLegalBalls(3);
        ChangeBowlerRequest request = new ChangeBowlerRequest(22L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> deliveryService.changeBowler(1L, request));
        assertTrue(ex.getMessage().contains("Bowler cannot be changed in the middle of an over"));
    }

    @Test
    @DisplayName("changeBowler - rejects consecutive overs by the same bowler")
    void changeBowler_consecutiveOver_rejected() {
        innings.setLegalBalls(6);
        ChangeBowlerRequest request = new ChangeBowlerRequest(21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamB, bowler1, "Bowler 1", 3, PlayingRole.BOWLER, false, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.changeBowler(1L, request));
        assertTrue(ex.getMessage().contains("Consecutive overs cannot be bowled by the same bowler"));
    }

    @Test
    @DisplayName("changeBowler - rejects consecutive overs when currentBowler was cleared at over end")
    void changeBowler_consecutiveOverAfterClearedBowler_rejected() {
        innings.setLegalBalls(6);
        innings.setCurrentBowler(null);
        ChangeBowlerRequest request = new ChangeBowlerRequest(21L);

        MatchDelivery lastBall = new MatchDelivery(innings, 6, 0, 6, bowler1, striker, nonStriker,
                0, ExtraType.NONE, 0, 0, true);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamB, bowler1, "Bowler 1", 3, PlayingRole.BOWLER, false, false)));
        when(matchDeliveryRepository.findTopByInnings_IdOrderByBallSequenceDesc(50L))
                .thenReturn(Optional.of(lastBall));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.changeBowler(1L, request));
        assertTrue(ex.getMessage().contains("Consecutive overs cannot be bowled by the same bowler"));
    }

    @Test
    @DisplayName("recordDelivery - fallOfWickets reports cumulative innings score and overs")
    void recordDelivery_fallOfWickets_cumulativeScore() {
        RecordDeliveryRequest request = new RecordDeliveryRequest(0, ExtraType.NONE, 0,
                true, DismissalType.BOWLED, 11L, null, 13L);

        MatchDelivery ball1 = new MatchDelivery(innings, 1, 0, 1, bowler1, striker, nonStriker,
                4, ExtraType.NONE, 0, 4, true);
        MatchDelivery ball2Wicket = new MatchDelivery(innings, 2, 0, 2, bowler1, striker, nonStriker,
                0, ExtraType.NONE, 0, 0, true, true, DismissalType.BOWLED, striker, null, nextBatter);

        innings.setLegalBalls(1);
        innings.setTotalRuns(4);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(striker));
        when(playerRepository.findById(13L)).thenReturn(Optional.of(nextBatter));
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 11L)).thenReturn(false);
        when(matchDeliveryRepository.existsByInnings_IdAndDismissedPlayer_Id(50L, 13L)).thenReturn(false);
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(new MatchPlayingXI(match, teamA, nextBatter, "Next Batter", 5, PlayingRole.BATSMAN, false, false)));
        when(matchDeliveryRepository.countByInnings_Id(50L)).thenReturn(1L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenReturn(ball2Wicket);
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(50L))
                .thenReturn(List.of(ball1, ball2Wicket));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        List<FallOfWicketResponse> fow = result.getInnings().getFallOfWickets();
        assertEquals(1, fow.size());
        assertEquals(1, fow.get(0).getWicketNumber());
        assertEquals(4, fow.get(0).getScore());
        assertEquals("0.2", fow.get(0).getOvers());
        assertEquals(11L, fow.get(0).getDismissedPlayerId());
        assertEquals("Striker", fow.get(0).getDismissedPlayerName());
    }

    @Test
    @DisplayName("recordDelivery - rejects invalid extras combination")
    void recordDelivery_invalidExtras() {
        RecordDeliveryRequest invalidWide = new RecordDeliveryRequest(2, ExtraType.WIDE, 1);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(innings));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deliveryService.recordDelivery(1L, invalidWide));
        assertTrue(ex.getMessage().contains("Wide delivery cannot have batter runs"));
    }

    @Test
    @DisplayName("recordDelivery - completes 2nd innings and match when target is chased")
    void recordDelivery_secondInningsTargetChased_completesMatch() {
        MatchInnings inn1 = new MatchInnings(match, 1, teamA, teamB, 2, null, null, null);
        inn1.setId(49L);
        inn1.setTotalRuns(20);
        inn1.setStatus(InningsStatus.COMPLETED);

        MatchInnings inn2 = new MatchInnings(match, 2, teamB, teamA, 2, bowler1, bowler2, striker);
        inn2.setId(51L);
        inn2.setTotalRuns(18);
        inn2.setTargetRuns(21);
        inn2.setLegalBalls(3);
        inn2.setWickets(2);
        inn2.setStatus(InningsStatus.IN_PROGRESS);

        RecordDeliveryRequest request = new RecordDeliveryRequest(4, ExtraType.NONE, 0);
        MatchDelivery savedDelivery = new MatchDelivery(inn2, 4, 0, 4, striker, bowler1, bowler2,
                4, ExtraType.NONE, 0, 4, true);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.findActiveInningsWithLock(1L)).thenReturn(Optional.of(inn2));
        when(matchDeliveryRepository.countByInnings_Id(51L)).thenReturn(3L);
        when(matchDeliveryRepository.saveAndFlush(any(MatchDelivery.class))).thenReturn(savedDelivery);
        when(matchInningsRepository.findByMatch_IdAndInningsNumber(1L, 1)).thenReturn(Optional.of(inn1));
        when(matchRepository.save(any(Match.class))).thenAnswer(i -> i.getArgument(0));
        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> i.getArgument(0));
        when(matchDeliveryRepository.findAllByInnings_IdOrderByBallSequenceAsc(51L)).thenReturn(List.of(savedDelivery));

        DeliveryResultResponse result = deliveryService.recordDelivery(1L, request);

        assertEquals(InningsStatus.COMPLETED, result.getInnings().getStatus());
        assertEquals(22, result.getInnings().getTotalRuns());
        assertEquals(MatchStatus.COMPLETED, match.getStatus());
        assertEquals(com.cricarena.backend.entity.enums.MatchResultType.WINNER_BY_WICKETS, match.getResultType());
        assertEquals(8, match.getResultMargin());
        assertEquals(teamB.getId(), match.getWinnerTeam().getId());
    }
}
