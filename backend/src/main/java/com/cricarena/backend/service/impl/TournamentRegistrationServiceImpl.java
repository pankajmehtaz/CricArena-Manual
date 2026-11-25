package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.response.TournamentRegistrationResponse;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.entity.Tournament;
import com.cricarena.backend.entity.TournamentRegistration;
import com.cricarena.backend.entity.enums.TournamentStatus;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TournamentRegistrationMapper;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentRepository;
import com.cricarena.backend.service.TournamentRegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TournamentRegistrationServiceImpl implements TournamentRegistrationService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentRegistrationMapper registrationMapper;

    public TournamentRegistrationServiceImpl(TournamentRepository tournamentRepository,
                                             TeamRepository teamRepository,
                                             TournamentRegistrationRepository registrationRepository,
                                             TournamentRegistrationMapper registrationMapper) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.registrationRepository = registrationRepository;
        this.registrationMapper = registrationMapper;
    }

    @Override
    public TournamentRegistrationResponse registerTeam(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found with id: " + tournamentId));

        if (tournament.getStatus() == TournamentStatus.COMPLETED || tournament.getStatus() == TournamentStatus.CANCELLED) {
            throw new IllegalStateException("Tournament is not eligible for team registration: status is " + tournament.getStatus());
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        if (!team.isActive()) {
            throw new IllegalStateException("Team is not active and cannot be registered: " + team.getName());
        }

        if (registrationRepository.existsByTournament_IdAndTeam_Id(tournamentId, teamId)) {
            throw new DuplicateResourceException(
                    "Team with id " + teamId + " is already registered in tournament with id " + tournamentId);
        }

        TournamentRegistration registration = new TournamentRegistration(tournament, team);
        TournamentRegistration saved = registrationRepository.save(registration);
        return registrationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentRegistrationResponse> getRegisteredTeams(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }

        return registrationRepository.findAllByTournament_Id(tournamentId)
                .stream()
                .map(registrationMapper::toResponse)
                .toList();
    }

    @Override
    public void removeRegistration(Long tournamentId, Long teamId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found with id: " + tournamentId);
        }
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        TournamentRegistration registration = registrationRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament id " + tournamentId + " and team id " + teamId));

        registrationRepository.delete(registration);
    }
}
