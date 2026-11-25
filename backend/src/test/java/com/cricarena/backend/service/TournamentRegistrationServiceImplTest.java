package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.TeamResponse;
import com.cricarena.backend.dto.response.TournamentRegistrationResponse;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.TournamentRegistration;
import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TournamentRegistrationMapper;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.service.impl.TournamentRegistrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentRegistrationServiceImplTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentRegistrationMapper registrationMapper;

    private TournamentRegistrationServiceImpl service;

    private Tournament activeTournament;
    private Team activeTeam;

    @BeforeEach
    void setUp() {
        service = new TournamentRegistrationServiceImpl(
                tournamentRepository,
                teamRepository,
                registrationRepository,
                registrationMapper
        );

        activeTournament = new Tournament("Premier Cup", "Desc", TournamentFormat.LEAGUE,
                LocalDate.now(), LocalDate.now().plusDays(10));
        activeTournament.setId(1L);
        activeTournament.setStatus(TournamentStatus.PUBLISHED);

        activeTeam = new Team("Lions XI", "LIONS", "Ground", "City", "lions@test.com", true, null);
        activeTeam.setId(10L);
    }

    @Test
    @DisplayName("Should successfully register an active team in an active tournament")
    void registerTeam_successful() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(activeTournament));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(activeTeam));
        when(registrationRepository.existsByTournament_IdAndTeam_Id(1L, 10L)).thenReturn(false);

        TournamentRegistration saved = new TournamentRegistration(activeTournament, activeTeam);
        saved.setId(100L);
        when(registrationRepository.save(any(TournamentRegistration.class))).thenReturn(saved);

        TournamentRegistrationResponse expectedResponse = new TournamentRegistrationResponse(
                100L, 1L, "Premier Cup", 10L, "Lions XI", LocalDateTime.now(), new TeamResponse()
        );
        when(registrationMapper.toResponse(saved)).thenReturn(expectedResponse);

        TournamentRegistrationResponse response = service.registerTeam(1L, 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getTournamentId());
        assertEquals(10L, response.getTeamId());
        verify(registrationRepository).save(any(TournamentRegistration.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when team is already registered in tournament")
    void registerTeam_duplicateRegistration_throwsDuplicateResourceException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(activeTournament));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(activeTeam));
        when(registrationRepository.existsByTournament_IdAndTeam_Id(1L, 10L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> service.registerTeam(1L, 10L)
        );

        assertTrue(ex.getMessage().contains("already registered"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tournament does not exist")
    void registerTeam_tournamentNotFound_throwsResourceNotFoundException() {
        when(tournamentRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.registerTeam(999L, 10L)
        );

        assertTrue(ex.getMessage().contains("Tournament not found"));
        verify(teamRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when team does not exist")
    void registerTeam_teamNotFound_throwsResourceNotFoundException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(activeTournament));
        when(teamRepository.findById(888L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.registerTeam(1L, 888L)
        );

        assertTrue(ex.getMessage().contains("Team not found"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when tournament is COMPLETED or CANCELLED")
    void registerTeam_ineligibleTournament_throwsIllegalStateException() {
        activeTournament.setStatus(TournamentStatus.COMPLETED);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(activeTournament));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTeam(1L, 10L)
        );

        assertTrue(ex.getMessage().contains("not eligible"));
        verify(teamRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when team is inactive")
    void registerTeam_inactiveTeam_throwsIllegalStateException() {
        activeTeam.setActive(false);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(activeTournament));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(activeTeam));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTeam(1L, 10L)
        );

        assertTrue(ex.getMessage().contains("Team is not active"));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return list of registered teams for a valid tournament")
    void getRegisteredTeams_successful() {
        when(tournamentRepository.existsById(1L)).thenReturn(true);

        TournamentRegistration reg = new TournamentRegistration(activeTournament, activeTeam);
        reg.setId(50L);
        when(registrationRepository.findAllByTournament_Id(1L)).thenReturn(List.of(reg));

        TournamentRegistrationResponse expected = new TournamentRegistrationResponse(
                50L, 1L, "Premier Cup", 10L, "Lions XI", LocalDateTime.now(), new TeamResponse()
        );
        when(registrationMapper.toResponse(reg)).thenReturn(expected);

        List<TournamentRegistrationResponse> list = service.getRegisteredTeams(1L);

        assertEquals(1, list.size());
        assertEquals(50L, list.get(0).getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when listing teams for non-existing tournament")
    void getRegisteredTeams_tournamentNotFound() {
        when(tournamentRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getRegisteredTeams(999L));
        verify(registrationRepository, never()).findAllByTournament_Id(any());
    }

    @Test
    @DisplayName("Should successfully remove a registration")
    void removeRegistration_successful() {
        when(tournamentRepository.existsById(1L)).thenReturn(true);
        when(teamRepository.existsById(10L)).thenReturn(true);

        TournamentRegistration reg = new TournamentRegistration(activeTournament, activeTeam);
        reg.setId(100L);
        when(registrationRepository.findByTournament_IdAndTeam_Id(1L, 10L)).thenReturn(Optional.of(reg));

        service.removeRegistration(1L, 10L);

        verify(registrationRepository).delete(reg);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when removing registration that does not exist")
    void removeRegistration_notFound() {
        when(tournamentRepository.existsById(1L)).thenReturn(true);
        when(teamRepository.existsById(10L)).thenReturn(true);
        when(registrationRepository.findByTournament_IdAndTeam_Id(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.removeRegistration(1L, 10L));
        verify(registrationRepository, never()).delete(any());
    }
}
