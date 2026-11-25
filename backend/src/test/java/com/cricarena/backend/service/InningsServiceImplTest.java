package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.StartInningsRequest;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchInnings;
import com.cricarena.backend.entity.MatchPlayingXI;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.BowlingStyle;
import com.cricarena.backend.entity.enums.InningsStatus;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TossDecision;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.mapper.InningsMapper;
import com.cricarena.backend.repository.MatchInningsRepository;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.service.impl.InningsServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InningsServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchInningsRepository matchInningsRepository;

    @Mock
    private MatchPlayingXIRepository matchPlayingXIRepository;

    @Mock
    private com.cricarena.backend.repository.MatchDeliveryRepository matchDeliveryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Spy
    private InningsMapper inningsMapper;

    @InjectMocks
    private InningsServiceImpl inningsService;

    private Match match;
    private Team teamA;
    private Team teamB;
    private Player playerA1;
    private Player playerA2;
    private Player playerB1;

    @BeforeEach
    void setUp() {
        Tournament tournament = new Tournament("Cup", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));
        tournament.setId(10L);

        College college = new College();
        college.setId(1L);
        college.setName("College");
        college.setCode("COL");
        college.setCity("City");
        college.setActive(true);

        teamA = new Team("Team A", "TA", "Ground A", "City", "ta@test.com", true, college);
        teamA.setId(100L);

        teamB = new Team("Team B", "TB", "Ground B", "City", "tb@test.com", true, college);
        teamB.setId(200L);

        match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0),
                "Ground", MatchFormat.T20, 20, MatchStatus.IN_PROGRESS);
        match.setId(1L);
        match.setTossWinnerTeam(teamA);
        match.setTossDecision(TossDecision.BAT);
        match.setBattingFirstTeam(teamA);
        match.setBowlingFirstTeam(teamB);

        playerA1 = new Player("Player A1", 1, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "pa1@test.com", "123", null, true, teamA);
        playerA1.setId(11L);

        playerA2 = new Player("Player A2", 2, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "pa2@test.com", "123", null, true, teamA);
        playerA2.setId(12L);

        playerB1 = new Player("Player B1", 1, PlayingRole.BOWLER, BattingStyle.RIGHT_HAND,
                BowlingStyle.RIGHT_ARM_FAST, LocalDate.of(2000, 1, 1), "pb1@test.com", "123", null, true, teamB);
        playerB1.setId(21L);
    }

    private MatchPlayingXI createXI(Match m, Team t, Player p) {
        return new MatchPlayingXI(m, t, p, p.getFullName(), p.getJerseyNumber(), p.getPlayingRole(), false, false);
    }

    @Test
    @DisplayName("startInnings - successfully initializes Innings 1 with correct batting and bowling teams")
    void startInnings_innings1_success() {
        StartInningsRequest request = new StartInningsRequest(1, 11L, 12L, 21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 1)).thenReturn(false);

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(createXI(match, teamA, playerA1), createXI(match, teamA, playerA2)));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(createXI(match, teamB, playerB1)));

        when(playerRepository.findById(11L)).thenReturn(Optional.of(playerA1));
        when(playerRepository.findById(12L)).thenReturn(Optional.of(playerA2));
        when(playerRepository.findById(21L)).thenReturn(Optional.of(playerB1));

        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> {
            MatchInnings saved = i.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        InningsResponse response = inningsService.startInnings(1L, request);

        assertNotNull(response);
        assertEquals(1, response.getInningsNumber());
        assertEquals(100L, response.getBattingTeamId());
        assertEquals(200L, response.getBowlingTeamId());
        assertEquals(11L, response.getCurrentStrikerId());
        assertEquals(12L, response.getCurrentNonStrikerId());
        assertEquals(21L, response.getCurrentBowlerId());
        assertEquals(20, response.getMaxOvers());
        assertEquals(InningsStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    @DisplayName("startInnings - rejects initialization when match is not IN_PROGRESS")
    void startInnings_matchNotScheduled() {
        match.setStatus(MatchStatus.SCHEDULED);
        StartInningsRequest request = new StartInningsRequest(1, 11L, 12L, 21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> inningsService.startInnings(1L, request));
        assertEquals("Innings can only be started when match is IN_PROGRESS; current status: SCHEDULED", ex.getMessage());
    }

    @Test
    @DisplayName("startInnings - rejects duplicate innings creation")
    void startInnings_duplicateInnings() {
        StartInningsRequest request = new StartInningsRequest(1, 11L, 12L, 21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 1)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> inningsService.startInnings(1L, request));
    }

    @Test
    @DisplayName("startInnings - rejects second innings when first innings is not COMPLETED")
    void startInnings_secondInningsBeforeFirstCompletes() {
        StartInningsRequest request = new StartInningsRequest(2, 21L, 22L, 11L);

        MatchInnings innings1 = new MatchInnings(match, 1, teamA, teamB, 20, playerA1, playerA2, playerB1);
        innings1.setStatus(InningsStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 2)).thenReturn(false);
        when(matchInningsRepository.findByMatch_IdAndInningsNumber(1L, 1)).thenReturn(Optional.of(innings1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> inningsService.startInnings(1L, request));
        assertEquals("First innings must be COMPLETED before second innings can be started; current status: IN_PROGRESS", ex.getMessage());
    }

    @Test
    @DisplayName("startInnings - successfully initializes Innings 2 with target = innings1.runs + 1")
    void startInnings_secondInnings_success() {
        Player playerB2 = new Player("Player B2", 2, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "pb2@test.com", "123", null, true, teamB);
        playerB2.setId(22L);

        StartInningsRequest request = new StartInningsRequest(2, 21L, 22L, 11L);

        MatchInnings innings1 = new MatchInnings(match, 1, teamA, teamB, 20, playerA1, playerA2, playerB1);
        innings1.setTotalRuns(165);
        innings1.setStatus(InningsStatus.COMPLETED);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 2)).thenReturn(false);
        when(matchInningsRepository.findByMatch_IdAndInningsNumber(1L, 1)).thenReturn(Optional.of(innings1));

        // In Innings 2, Batting is Team B (200), Bowling is Team A (100)
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L))
                .thenReturn(List.of(createXI(match, teamB, playerB1), createXI(match, teamB, playerB2)));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(createXI(match, teamA, playerA1)));

        when(playerRepository.findById(21L)).thenReturn(Optional.of(playerB1));
        when(playerRepository.findById(22L)).thenReturn(Optional.of(playerB2));
        when(playerRepository.findById(11L)).thenReturn(Optional.of(playerA1));

        when(matchInningsRepository.saveAndFlush(any(MatchInnings.class))).thenAnswer(i -> {
            MatchInnings saved = i.getArgument(0);
            saved.setId(102L);
            return saved;
        });

        InningsResponse response = inningsService.startInnings(1L, request);

        assertNotNull(response);
        assertEquals(2, response.getInningsNumber());
        assertEquals(200L, response.getBattingTeamId());
        assertEquals(100L, response.getBowlingTeamId());
        assertEquals(166, response.getTargetRuns()); // 165 + 1
        assertEquals(InningsStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    @DisplayName("startInnings - rejects when striker and non-striker are identical")
    void startInnings_sameStrikerAndNonStriker() {
        StartInningsRequest request = new StartInningsRequest(1, 11L, 11L, 21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 1)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> inningsService.startInnings(1L, request));
        assertEquals("Striker and non-striker cannot be the same player", ex.getMessage());
    }

    @Test
    @DisplayName("startInnings - rejects when player is not in Playing XI")
    void startInnings_playerNotInPlayingXI() {
        StartInningsRequest request = new StartInningsRequest(1, 11L, 12L, 21L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchInningsRepository.existsByMatch_IdAndInningsNumber(1L, 1)).thenReturn(false);

        // Striker 11 is in XI, but 12 is not
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L))
                .thenReturn(List.of(createXI(match, teamA, playerA1)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> inningsService.startInnings(1L, request));
        assertEquals("Non-striker with id 12 is not in the Playing XI for team 100 in match 1", ex.getMessage());
    }
}
