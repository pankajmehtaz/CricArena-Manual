package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.PlayingXISelectionRequest;
import com.cricarena.backend.dto.response.MatchPlayerResponse;
import com.cricarena.backend.dto.response.MatchPlayingXIResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Match;
import com.cricarena.backend.entity.MatchPlayingXI;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.BowlingStyle;
import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.MatchPlayingXIMapper;
import com.cricarena.backend.repository.MatchPlayingXIRepository;
import com.cricarena.backend.repository.MatchRepository;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.service.impl.MatchPlayingXIServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchPlayingXIServiceImplTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MatchPlayingXIRepository matchPlayingXIRepository;

    @Mock
    private com.cricarena.backend.repository.TournamentSquadRepository tournamentSquadRepository;

    @Spy
    private MatchPlayingXIMapper matchPlayingXIMapper = new MatchPlayingXIMapper();

    @InjectMocks
    private MatchPlayingXIServiceImpl matchPlayingXIService;

    private Match match;
    private Team teamA;
    private Team teamB;
    private List<Player> teamAPlayers;
    private List<Long> teamAPlayerIds;

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

        teamAPlayers = new ArrayList<>();
        teamAPlayerIds = new ArrayList<>();
        for (long i = 1; i <= 11; i++) {
            Player p = new Player("Player " + i, (int) i, PlayingRole.BATSMAN, BattingStyle.RIGHT_HAND,
                    BowlingStyle.NONE, LocalDate.of(2000, 1, 1), "p" + i + "@test.com", "123", null, true, teamA);
            p.setId(i);
            teamAPlayers.add(p);
            teamAPlayerIds.add(i);
        }
    }

    @Test
    @DisplayName("setPlayingXI - successfully sets 11 players with captain, keeper and snapshots")
    void setPlayingXI_success() {
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(
                100L, teamAPlayerIds, 1L, 2L
        );

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(List.of());

        for (Player p : teamAPlayers) {
            when(playerRepository.findById(p.getId())).thenReturn(Optional.of(p));
        }

        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 100L)).thenAnswer(i -> {
            List<MatchPlayingXI> list = new ArrayList<>();
            for (Player p : teamAPlayers) {
                list.add(new MatchPlayingXI(match, teamA, p, p.getFullName(), p.getJerseyNumber(),
                        p.getPlayingRole(), p.getId() == 1L, p.getId() == 2L));
            }
            return list;
        });

        MatchPlayingXIResponse response = matchPlayingXIService.setPlayingXI(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getMatchId());
        assertEquals(11, response.getTeamAPlayers().size());
        assertTrue(response.getTeamAPlayers().stream().anyMatch(MatchPlayerResponse::isCaptain));
        assertTrue(response.getTeamAPlayers().stream().anyMatch(MatchPlayerResponse::isWicketkeeper));

        verify(matchPlayingXIRepository).deleteAllByMatch_IdAndTeam_Id(1L, 100L);
        verify(matchPlayingXIRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when fewer than 11 players provided")
    void setPlayingXI_fewerThan11Players() {
        List<Long> tenIds = teamAPlayerIds.subList(0, 10);
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, tenIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when duplicate player IDs provided")
    void setPlayingXI_duplicatePlayerIds() {
        List<Long> duplicateIds = new ArrayList<>(teamAPlayerIds.subList(0, 10));
        duplicateIds.add(1L); // duplicate player 1
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, duplicateIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when captain not among the 11 players")
    void setPlayingXI_captainNotInSquad() {
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 999L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when wicketkeeper not among the 11 players")
    void setPlayingXI_wicketkeeperNotInSquad() {
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 1L, 999L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalStateException when a selected player is inactive")
    void setPlayingXI_inactivePlayer() {
        teamAPlayers.get(0).setActive(false);
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(List.of());
        when(playerRepository.findById(1L)).thenReturn(Optional.of(teamAPlayers.get(0)));

        assertThrows(IllegalStateException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalStateException when a player belongs to another team")
    void setPlayingXI_playerBelongsToAnotherTeam() {
        teamAPlayers.get(0).setTeam(teamB); // player belongs to teamB
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(List.of());
        when(playerRepository.findById(1L)).thenReturn(Optional.of(teamAPlayers.get(0)));

        assertThrows(IllegalStateException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when team is not participating in the match")
    void setPlayingXI_nonParticipatingTeam() {
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(999L, teamAPlayerIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalStateException when match is not SCHEDULED (locked)")
    void setPlayingXI_lockedWhenNotScheduled() {
        match.setStatus(MatchStatus.IN_PROGRESS);
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 1L, 2L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(IllegalStateException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("setPlayingXI - throws IllegalArgumentException when player is already selected in opponent XI")
    void setPlayingXI_playerInOpponentXI() {
        PlayingXISelectionRequest request = new PlayingXISelectionRequest(100L, teamAPlayerIds, 1L, 2L);

        MatchPlayingXI opponentPlayer = new MatchPlayingXI(match, teamB, teamAPlayers.get(0), "Player 1", 1, PlayingRole.BATSMAN, false, false);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchPlayingXIRepository.findAllByMatch_IdAndTeam_Id(1L, 200L)).thenReturn(List.of(opponentPlayer));

        assertThrows(IllegalArgumentException.class, () -> matchPlayingXIService.setPlayingXI(1L, request));
        verify(matchPlayingXIRepository, never()).saveAllAndFlush(any());
    }
}
