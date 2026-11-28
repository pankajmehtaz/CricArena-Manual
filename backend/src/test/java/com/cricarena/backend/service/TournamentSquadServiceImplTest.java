package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.TournamentSquadResponse;
import com.cricarena.backend.entity.College;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.TournamentRegistration;
import com.cricarena.backend.entity.TournamentSquad;
import com.cricarena.backend.entity.enums.PlayingRole;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TournamentSquadMapper;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentSquadRepository;
import com.cricarena.backend.service.impl.TournamentSquadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentSquadServiceImplTest {

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentSquadRepository tournamentSquadRepository;

    @Mock
    private PlayerRepository playerRepository;

    private TournamentSquadMapper squadMapper;
    private TournamentSquadServiceImpl squadService;

    private Tournament tournament;
    private Team team;
    private TournamentRegistration registration;
    private Player player1;

    @BeforeEach
    void setUp() {
        squadMapper = new TournamentSquadMapper();
        squadService = new TournamentSquadServiceImpl(
                registrationRepository,
                tournamentSquadRepository,
                playerRepository,
                squadMapper
        );

        College college = new College();
        college.setId(1L);

        tournament = new Tournament("Premier Cup", "Annual Cup", TournamentFormat.LEAGUE,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 20));
        tournament.setId(10L);
        tournament.setRegistrationOpensAt(LocalDateTime.now().minusDays(5));
        tournament.setRegistrationDeadline(LocalDateTime.now().plusDays(5));

        team = new Team("Lions XI", "LXI", "Ground", "Delhi", "lions@iitd.ac.in", true, college);
        team.setId(20L);

        registration = new TournamentRegistration(tournament, team);
        registration.setId(100L);

        player1 = new Player();
        player1.setId(500L);
        player1.setFullName("Virat Kohli");
        player1.setJerseyNumber(18);
        player1.setPlayingRole(PlayingRole.BATSMAN);
        player1.setActive(true);
        player1.setTeam(team);
    }

    @Test
    @DisplayName("Add player: successfully adds player to squad")
    void addPlayerToSquad_success() {
        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(tournamentSquadRepository.countByTournamentRegistration_Id(100L)).thenReturn(10L);
        when(tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(100L, 500L)).thenReturn(false);
        when(playerRepository.findById(500L)).thenReturn(Optional.of(player1));
        when(tournamentSquadRepository.existsByTournament_IdAndPlayer_Id(10L, 500L)).thenReturn(false);

        TournamentSquad savedSquad = new TournamentSquad(registration, tournament, player1, "Virat Kohli", 18, "BATSMAN");
        savedSquad.setId(1L);
        when(tournamentSquadRepository.save(any(TournamentSquad.class))).thenReturn(savedSquad);
        when(tournamentSquadRepository.findAllByTournamentRegistration_Id(100L)).thenReturn(List.of(savedSquad));

        TournamentSquadResponse response = squadService.addPlayerToSquad(10L, 20L, 500L);

        assertNotNull(response);
        assertEquals(10L, response.getTournamentId());
        assertEquals(20L, response.getTeamId());
        assertEquals(1, response.getSquadSize());
        assertEquals(15, response.getMaxSquadSize());
        assertEquals("Virat Kohli", response.getMembers().get(0).getPlayerName());
    }

    @Test
    @DisplayName("Add player: throws IllegalStateException when squad reaches 15 players")
    void addPlayerToSquad_quotaExceeded_throwsIllegalStateException() {
        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(tournamentSquadRepository.countByTournamentRegistration_Id(100L)).thenReturn(15L);

        assertThrows(IllegalStateException.class,
                () -> squadService.addPlayerToSquad(10L, 20L, 500L));
    }

    @Test
    @DisplayName("Add player: throws DuplicateResourceException if player is in another team's squad for same tournament")
    void addPlayerToSquad_tournamentWideDuplicate_throwsDuplicateResourceException() {
        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(tournamentSquadRepository.countByTournamentRegistration_Id(100L)).thenReturn(5L);
        when(tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(100L, 500L)).thenReturn(false);
        when(playerRepository.findById(500L)).thenReturn(Optional.of(player1));
        when(tournamentSquadRepository.existsByTournament_IdAndPlayer_Id(10L, 500L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> squadService.addPlayerToSquad(10L, 20L, 500L));
    }

    @Test
    @DisplayName("Add player: throws IllegalStateException if player belongs to a different team")
    void addPlayerToSquad_playerOfDifferentTeam_throwsIllegalStateException() {
        Team otherTeam = new Team();
        otherTeam.setId(99L);
        player1.setTeam(otherTeam);

        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(tournamentSquadRepository.countByTournamentRegistration_Id(100L)).thenReturn(5L);
        when(tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(100L, 500L)).thenReturn(false);
        when(playerRepository.findById(500L)).thenReturn(Optional.of(player1));

        assertThrows(IllegalStateException.class,
                () -> squadService.addPlayerToSquad(10L, 20L, 500L));
    }

    @Test
    @DisplayName("Add player: throws IllegalStateException when registration deadline has passed")
    void addPlayerToSquad_deadlinePassed_throwsIllegalStateException() {
        tournament.setRegistrationDeadline(LocalDateTime.now().minusHours(1));

        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class,
                () -> squadService.addPlayerToSquad(10L, 20L, 500L));
    }

    @Test
    @DisplayName("Set squad: batch sets up to 15 players")
    void setSquad_success() {
        List<Long> playerIds = List.of(500L);

        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(playerRepository.findById(500L)).thenReturn(Optional.of(player1));
        when(tournamentSquadRepository.existsByTournament_IdAndPlayer_Id(10L, 500L)).thenReturn(false);

        TournamentSquad squad = new TournamentSquad(registration, tournament, player1, "Virat Kohli", 18, "BATSMAN");
        when(tournamentSquadRepository.findAllByTournamentRegistration_Id(100L)).thenReturn(List.of(squad));

        TournamentSquadResponse response = squadService.setSquad(10L, 20L, playerIds);

        assertNotNull(response);
        assertEquals(1, response.getSquadSize());
        verify(tournamentSquadRepository).deleteAllByTournamentRegistration_Id(100L);
    }

    @Test
    @DisplayName("Set squad: throws IllegalStateException if list has more than 15 players")
    void setSquad_exceeds15_throwsIllegalStateException() {
        List<Long> playerIds = new ArrayList<>();
        for (long i = 1; i <= 16; i++) {
            playerIds.add(i);
        }

        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));

        assertThrows(IllegalStateException.class,
                () -> squadService.setSquad(10L, 20L, playerIds));
    }

    @Test
    @DisplayName("Remove player: successfully removes squad member")
    void removePlayerFromSquad_success() {
        when(registrationRepository.findByTournament_IdAndTeam_IdForUpdate(10L, 20L))
                .thenReturn(Optional.of(registration));
        when(tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(100L, 500L)).thenReturn(true);

        squadService.removePlayerFromSquad(10L, 20L, 500L);

        verify(tournamentSquadRepository).deleteByTournamentRegistration_IdAndPlayer_Id(100L, 500L);
    }
}
