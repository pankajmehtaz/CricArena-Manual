package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.TossRequest;
import com.cricarena.backend.dto.response.TossResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TossDecision;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.service.impl.TossServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TossServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TossServiceImpl tossService;

    private Match match;
    private Team teamA;
    private Team teamB;

    @BeforeEach
    void setUp() {
        College college = new College();
        college.setId(1L);

        Tournament tournament = new Tournament("Cup", "Desc", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 15));
        tournament.setId(10L);

        teamA = new Team("Lions XI", "LIONS", "Ground A", "Delhi", "lions@test.com", true, college);
        teamA.setId(100L);

        teamB = new Team("Tigers XI", "TIGERS", "Ground B", "Delhi", "tigers@test.com", true, college);
        teamB.setId(200L);

        match = new Match(tournament, teamA, teamB, LocalDateTime.of(2026, 12, 5, 14, 0), "Ground", MatchFormat.T20);
        match.setId(1L);
        match.setStatus(MatchStatus.SCHEDULED);
    }

    @Test
    @DisplayName("recordToss - winner chooses BAT: winner bats first, other bowls first")
    void recordToss_batDecision() {
        TossRequest request = new TossRequest(100L, TossDecision.BAT);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.saveAndFlush(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        TossResponse response = tossService.recordToss(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getTossWinnerTeamId());
        assertEquals(TossDecision.BAT, response.getDecision());
        assertEquals(100L, response.getBattingFirstTeamId());
        assertEquals(200L, response.getBowlingFirstTeamId());

        verify(matchRepository).saveAndFlush(match);
    }

    @Test
    @DisplayName("recordToss - winner chooses BOWL: other bats first, winner bowls first")
    void recordToss_bowlDecision() {
        TossRequest request = new TossRequest(100L, TossDecision.BOWL);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.saveAndFlush(any(Match.class))).thenAnswer(i -> i.getArgument(0));

        TossResponse response = tossService.recordToss(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getTossWinnerTeamId());
        assertEquals(TossDecision.BOWL, response.getDecision());
        assertEquals(200L, response.getBattingFirstTeamId());
        assertEquals(100L, response.getBowlingFirstTeamId());

        verify(matchRepository).saveAndFlush(match);
    }

    @Test
    @DisplayName("recordToss - throws IllegalArgumentException when toss winner is not participating in match")
    void recordToss_invalidWinnerTeam() {
        TossRequest request = new TossRequest(999L, TossDecision.BAT);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> tossService.recordToss(1L, request));
        verify(matchRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("recordToss - throws IllegalStateException when match is not SCHEDULED")
    void recordToss_lockedWhenNotScheduled() {
        match.setStatus(MatchStatus.IN_PROGRESS);
        TossRequest request = new TossRequest(100L, TossDecision.BAT);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalStateException.class, () -> tossService.recordToss(1L, request));
        verify(matchRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("getToss - returns toss response when recorded")
    void getToss_success() {
        match.setTossWinnerTeam(teamA);
        match.setTossDecision(TossDecision.BAT);
        match.setBattingFirstTeam(teamA);
        match.setBowlingFirstTeam(teamB);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        TossResponse response = tossService.getToss(1L);
        assertNotNull(response);
        assertEquals(100L, response.getTossWinnerTeamId());
        assertEquals(TossDecision.BAT, response.getDecision());
    }

    @Test
    @DisplayName("getToss - throws ResourceNotFoundException when toss not yet recorded")
    void getToss_notYetRecorded() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(ResourceNotFoundException.class, () -> tossService.getToss(1L));
    }
}
