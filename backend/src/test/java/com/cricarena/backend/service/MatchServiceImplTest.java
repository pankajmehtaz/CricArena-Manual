package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.response.MatchResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchMapper;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.service.impl.MatchServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TournamentRegistrationRepository tournamentRegistrationRepository;

    @Mock
    private com.cricarena.backend.repository.MatchPlayingXIRepository matchPlayingXIRepository;

    @Spy
    private MatchMapper matchMapper = new MatchMapper();

    @InjectMocks
    private MatchServiceImpl matchService;

    private Tournament tournament;
    private Team teamA;
    private Team teamB;
    private College college;

    @BeforeEach
    void setUp() {
        college = new College();
        college.setId(1L);
        college.setName("IIT Delhi");

        tournament = new Tournament("CricArena Cup 2026", "Annual cricket",
                TournamentFormat.LEAGUE, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));
        tournament.setId(10L);
        tournament.setStatus(TournamentStatus.SCHEDULED);

        teamA = new Team("Lions XI", "LIONS", "Oval", "Delhi", "lions@test.com", true, college);
        teamA.setId(100L);

        teamB = new Team("Tigers XI", "TIGERS", "East", "Delhi", "tigers@test.com", true, college);
        teamB.setId(200L);
    }

    private MatchRequest createValidRequest() {
        return new MatchRequest(
                10L,
                100L,
                200L,
                LocalDateTime.of(2026, 12, 5, 14, 0),
                "Central Cricket Ground",
                MatchFormat.T20
        );
    }

    @Test
    @DisplayName("createMatch - succeeds with valid request and registered active teams")
    void createMatch_success() {
        MatchRequest request = createValidRequest();

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(200L)).thenReturn(Optional.of(teamB));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            m.setId(1L);
            m.setCreatedAt(LocalDateTime.now());
            m.setUpdatedAt(LocalDateTime.now());
            return m;
        });

        MatchResponse response = matchService.createMatch(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getTournamentId());
        assertEquals(100L, response.getTeamAId());
        assertEquals(200L, response.getTeamBId());
        assertEquals(MatchStatus.SCHEDULED, response.getStatus());
        assertEquals(MatchFormat.T20, response.getFormat());
        assertEquals("Central Cricket Ground", response.getVenue());

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("createMatch - throws ResourceNotFoundException when tournament does not exist")
    void createMatch_tournamentNotFound() {
        MatchRequest request = createValidRequest();
        when(tournamentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(request));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMatch - throws IllegalStateException when tournament is COMPLETED or CANCELLED")
    void createMatch_tournamentCompletedOrCancelled() {
        MatchRequest request = createValidRequest();
        tournament.setStatus(TournamentStatus.COMPLETED);
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));

        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));

        tournament.setStatus(TournamentStatus.CANCELLED);
        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMatch - throws IllegalArgumentException when team A and team B are identical")
    void createMatch_sameTeamSelectedTwice() {
        MatchRequest request = createValidRequest();
        request.setTeamBId(100L); // same as teamAId

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(teamA));

        assertThrows(IllegalArgumentException.class, () -> matchService.createMatch(request));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMatch - throws IllegalStateException when Team A or Team B is inactive")
    void createMatch_inactiveTeam() {
        MatchRequest request = createValidRequest();
        teamA.setActive(false);

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(200L)).thenReturn(Optional.of(teamB));

        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));

        teamA.setActive(true);
        teamB.setActive(false);
        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));

        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMatch - throws IllegalStateException when Team A or Team B is not registered")
    void createMatch_unregisteredTeam() {
        MatchRequest request = createValidRequest();

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(200L)).thenReturn(Optional.of(teamB));

        // Team A not registered
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));

        // Team A registered, Team B not registered
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> matchService.createMatch(request));

        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMatch - throws IllegalArgumentException when match date is outside tournament window")
    void createMatch_dateOutsideTournamentWindow() {
        MatchRequest request = createValidRequest();

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(100L)).thenReturn(Optional.of(teamA));
        when(teamRepository.findById(200L)).thenReturn(Optional.of(teamB));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);

        // Before tournament start date (start is 2026-12-01)
        request.setMatchDateTime(LocalDateTime.of(2026, 11, 30, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> matchService.createMatch(request));

        // After tournament end date (end is 2026-12-15)
        request.setMatchDateTime(LocalDateTime.of(2026, 12, 16, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> matchService.createMatch(request));

        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("getMatchById - returns match response when found")
    void getMatchById_success() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Ground A", MatchFormat.T20);
        match.setId(5L);
        match.setCreatedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        when(matchRepository.findById(5L)).thenReturn(Optional.of(match));

        MatchResponse response = matchService.getMatchById(5L);
        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Ground A", response.getVenue());
    }

    @Test
    @DisplayName("getMatchById - throws ResourceNotFoundException when not found")
    void getMatchById_notFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> matchService.getMatchById(99L));
    }

    @Test
    @DisplayName("getMatchesByTournament - returns tournament matches ordered by date")
    void getMatchesByTournament_success() {
        Match match1 = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 2, 10, 0), "Ground 1", MatchFormat.T20);
        match1.setId(1L);
        Match match2 = new Match(tournament, teamB, teamA, LocalDateTime.of(2026, 12, 4, 14, 0), "Ground 2", MatchFormat.T20);
        match2.setId(2L);

        when(tournamentRepository.existsById(10L)).thenReturn(true);
        when(matchRepository.findAllByTournament_IdOrderByMatchDateTimeAsc(10L)).thenReturn(List.of(match1, match2));

        List<MatchResponse> results = matchService.getMatchesByTournament(10L);
        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }

    @Test
    @DisplayName("updateMatch - updates venue, date, format, and status with valid transitions")
    void updateMatch_success() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Old Venue", MatchFormat.T20);
        match.setId(1L);
        LocalDateTime initialCreatedAt = LocalDateTime.of(2026, 11, 20, 10, 0);
        match.setCreatedAt(initialCreatedAt);
        match.setUpdatedAt(initialCreatedAt);

        MatchRequest updateRequest = new MatchRequest(
                10L, 100L, 200L,
                LocalDateTime.of(2026, 12, 8, 15, 0),
                "New Venue", MatchFormat.ODI, MatchStatus.SCHEDULED
        );

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchRepository.saveAndFlush(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        MatchResponse response = matchService.updateMatch(1L, updateRequest);

        assertNotNull(response);
        assertEquals("New Venue", response.getVenue());
        assertEquals(MatchFormat.ODI, response.getFormat());
        assertEquals(MatchStatus.SCHEDULED, response.getStatus());
        assertEquals(initialCreatedAt, response.getCreatedAt()); // CreatedAt preserved
    }

    @Test
    @DisplayName("updateMatch - throws IllegalStateException when changing teams on non-SCHEDULED match")
    void updateMatch_cannotChangeTeamsWhenNotScheduled() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.IN_PROGRESS);

        MatchRequest updateRequest = new MatchRequest(
                10L, 999L, 200L, // changed team A
                LocalDateTime.of(2026, 12, 5, 14, 0),
                "Venue", MatchFormat.T20
        );

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalStateException.class, () -> matchService.updateMatch(1L, updateRequest));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMatch - throws IllegalStateException on invalid status transitions")
    void updateMatch_invalidStatusTransitions() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.COMPLETED); // terminal status

        MatchRequest updateRequest = new MatchRequest(
                10L, 100L, 200L,
                LocalDateTime.of(2026, 12, 5, 14, 0),
                "Venue", MatchFormat.T20, MatchStatus.IN_PROGRESS
        );

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> matchService.updateMatch(1L, updateRequest));
        verify(matchRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteMatch - deletes SCHEDULED match")
    void deleteMatch_success() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        matchService.deleteMatch(1L);

        verify(matchRepository).delete(match);
    }

    @Test
    @DisplayName("deleteMatch - throws IllegalStateException when match is not SCHEDULED")
    void deleteMatch_notScheduled() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalStateException.class, () -> matchService.deleteMatch(1L));
        verify(matchRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateMatch - throws IllegalStateException when starting match without Team A Playing XI")
    void updateMatch_startRejectedWithoutTeamAXI() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);

        MatchRequest request = new MatchRequest(10L, 100L, 200L, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20, MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 100L)).thenReturn(10L); // only 10 players

        assertThrows(IllegalStateException.class, () -> matchService.updateMatch(1L, request));
    }

    @Test
    @DisplayName("updateMatch - throws IllegalStateException when starting match without Team B Playing XI")
    void updateMatch_startRejectedWithoutTeamBXI() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);

        MatchRequest request = new MatchRequest(10L, 100L, 200L, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20, MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 100L)).thenReturn(11L);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(0L); // missing

        assertThrows(IllegalStateException.class, () -> matchService.updateMatch(1L, request));
    }

    @Test
    @DisplayName("updateMatch - throws IllegalStateException when starting match without Toss recorded")
    void updateMatch_startRejectedWithoutToss() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);
        // Toss not recorded

        MatchRequest request = new MatchRequest(10L, 100L, 200L, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20, MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 100L)).thenReturn(11L);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(11L);

        assertThrows(IllegalStateException.class, () -> matchService.updateMatch(1L, request));
    }

    @Test
    @DisplayName("updateMatch - successfully starts match when both XIs and Toss are complete")
    void updateMatch_startSuccessWithBothXIsAndToss() {
        Match match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);
        match.setTossWinnerTeam(teamA);
        match.setTossDecision(com.cricarena.backend.entity.enums.TossDecision.BAT);
        match.setBattingFirstTeam(teamA);
        match.setBowlingFirstTeam(teamB);

        MatchRequest request = new MatchRequest(10L, 100L, 200L, LocalDateTime.of(2026, 12, 5, 14, 0), "Venue", MatchFormat.T20, MatchStatus.IN_PROGRESS);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 100L)).thenReturn(true);
        when(tournamentRegistrationRepository.existsByTournament_IdAndTeam_Id(10L, 200L)).thenReturn(true);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 100L)).thenReturn(11L);
        when(matchPlayingXIRepository.countByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(11L);
        when(matchRepository.saveAndFlush(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        MatchResponse response = matchService.updateMatch(1L, request);
        assertNotNull(response);
        assertEquals(MatchStatus.IN_PROGRESS, response.getStatus());
        assertEquals(100L, response.getTossWinnerTeamId());
    }

    @Test
    @DisplayName("resolveMaxOvers - validates T20, ODI, CUSTOM and TEST formats")
    void resolveMaxOvers_validation() {
        assertEquals(20, Match.resolveMaxOvers(MatchFormat.T20, null));
        assertEquals(50, Match.resolveMaxOvers(MatchFormat.ODI, null));
        assertEquals(15, Match.resolveMaxOvers(MatchFormat.CUSTOM, 15));

        // Custom requires positive maxOvers
        IllegalArgumentException exCustom = assertThrows(IllegalArgumentException.class,
                () -> Match.resolveMaxOvers(MatchFormat.CUSTOM, null));
        assertEquals("CUSTOM match format requires a positive maxOvers value", exCustom.getMessage());

        // TEST is not supported
        IllegalArgumentException exTest = assertThrows(IllegalArgumentException.class,
                () -> Match.resolveMaxOvers(MatchFormat.TEST, null));
        assertEquals("TEST match scoring is not supported in this two-innings limited-overs MVP", exTest.getMessage());
    }
}
