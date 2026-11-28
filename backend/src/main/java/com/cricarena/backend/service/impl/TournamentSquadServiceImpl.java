package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.response.TournamentSquadMemberResponse;
import com.cricarena.backend.dto.response.TournamentSquadResponse;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.TournamentRegistration;
import com.cricarena.backend.entity.TournamentSquad;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.TournamentSquadMapper;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TournamentRegistrationRepository;
import com.cricarena.backend.repository.TournamentSquadRepository;
import com.cricarena.backend.service.TournamentSquadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TournamentSquadServiceImpl implements TournamentSquadService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentSquadRepository tournamentSquadRepository;
    private final PlayerRepository playerRepository;
    private final TournamentSquadMapper squadMapper;

    public TournamentSquadServiceImpl(TournamentRegistrationRepository registrationRepository,
                                      TournamentSquadRepository tournamentSquadRepository,
                                      PlayerRepository playerRepository,
                                      TournamentSquadMapper squadMapper) {
        this.registrationRepository = registrationRepository;
        this.tournamentSquadRepository = tournamentSquadRepository;
        this.playerRepository = playerRepository;
        this.squadMapper = squadMapper;
    }

    @Override
    public TournamentSquadResponse addPlayerToSquad(Long tournamentId, Long teamId, Long playerId) {
        TournamentRegistration reg = registrationRepository.findByTournament_IdAndTeam_IdForUpdate(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament id " + tournamentId + " and team id " + teamId));

        validateSquadWindow(reg);

        long currentCount = tournamentSquadRepository.countByTournamentRegistration_Id(reg.getId());
        if (currentCount >= 15) {
            throw new IllegalStateException("Tournament squad limit of 15 players reached for this team.");
        }

        if (tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(reg.getId(), playerId)) {
            throw new DuplicateResourceException("Player is already in this team's tournament squad.");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        if (!player.isActive()) {
            throw new IllegalStateException("Cannot add inactive player to tournament squad: " + player.getFullName());
        }

        if (!player.getTeam().getId().equals(teamId)) {
            throw new IllegalStateException("Player " + player.getFullName() + " does not belong to team: " + reg.getTeam().getName());
        }

        if (tournamentSquadRepository.existsByTournament_IdAndPlayer_Id(tournamentId, playerId)) {
            throw new DuplicateResourceException("Player " + player.getFullName() + " is already selected in another squad for this tournament.");
        }

        TournamentSquad squad = new TournamentSquad(
                reg,
                reg.getTournament(),
                player,
                player.getFullName(),
                player.getJerseyNumber(),
                player.getPlayingRole() != null ? player.getPlayingRole().name() : "ALL_ROUNDER"
        );

        tournamentSquadRepository.save(squad);
        return buildSquadResponse(reg);
    }

    @Override
    public TournamentSquadResponse setSquad(Long tournamentId, Long teamId, List<Long> playerIds) {
        TournamentRegistration reg = registrationRepository.findByTournament_IdAndTeam_IdForUpdate(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament id " + tournamentId + " and team id " + teamId));

        validateSquadWindow(reg);

        if (playerIds == null || playerIds.isEmpty()) {
            throw new IllegalArgumentException("Player IDs list cannot be empty.");
        }

        if (playerIds.size() > 15) {
            throw new IllegalStateException("Tournament squad cannot exceed 15 players; requested size: " + playerIds.size());
        }

        Set<Long> uniqueIds = new HashSet<>(playerIds);
        if (uniqueIds.size() != playerIds.size()) {
            throw new IllegalArgumentException("Duplicate player IDs provided in squad selection.");
        }

        tournamentSquadRepository.deleteAllByTournamentRegistration_Id(reg.getId());
        tournamentSquadRepository.flush();

        List<TournamentSquad> newMembers = new ArrayList<>();
        for (Long pId : playerIds) {
            Player player = playerRepository.findById(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + pId));

            if (!player.isActive()) {
                throw new IllegalStateException("Cannot add inactive player: " + player.getFullName());
            }

            if (!player.getTeam().getId().equals(teamId)) {
                throw new IllegalStateException("Player " + player.getFullName() + " does not belong to team: " + reg.getTeam().getName());
            }

            if (tournamentSquadRepository.existsByTournament_IdAndPlayer_Id(tournamentId, pId)) {
                throw new DuplicateResourceException("Player " + player.getFullName() + " is already enrolled in another squad for this tournament.");
            }

            newMembers.add(new TournamentSquad(
                    reg,
                    reg.getTournament(),
                    player,
                    player.getFullName(),
                    player.getJerseyNumber(),
                    player.getPlayingRole() != null ? player.getPlayingRole().name() : "ALL_ROUNDER"
            ));
        }

        tournamentSquadRepository.saveAll(newMembers);
        return buildSquadResponse(reg);
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentSquadResponse getSquad(Long tournamentId, Long teamId) {
        TournamentRegistration reg = registrationRepository.findByTournament_IdAndTeam_Id(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament id " + tournamentId + " and team id " + teamId));

        return buildSquadResponse(reg);
    }

    @Override
    public void removePlayerFromSquad(Long tournamentId, Long teamId, Long playerId) {
        TournamentRegistration reg = registrationRepository.findByTournament_IdAndTeam_IdForUpdate(tournamentId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found for tournament id " + tournamentId + " and team id " + teamId));

        validateSquadWindow(reg);

        if (!tournamentSquadRepository.existsByTournamentRegistration_IdAndPlayer_Id(reg.getId(), playerId)) {
            throw new ResourceNotFoundException("Player is not in this tournament squad.");
        }

        tournamentSquadRepository.deleteByTournamentRegistration_IdAndPlayer_Id(reg.getId(), playerId);
    }

    private void validateSquadWindow(TournamentRegistration reg) {
        LocalDateTime now = LocalDateTime.now();
        if (reg.getTournament().getRegistrationDeadline() != null && now.isAfter(reg.getTournament().getRegistrationDeadline())) {
            throw new IllegalStateException("Squad changes are closed: registration deadline has passed.");
        }
    }

    private TournamentSquadResponse buildSquadResponse(TournamentRegistration reg) {
        List<TournamentSquad> members = tournamentSquadRepository.findAllByTournamentRegistration_Id(reg.getId());

        TournamentSquadResponse response = new TournamentSquadResponse();
        response.setRegistrationId(reg.getId());
        response.setTournamentId(reg.getTournament().getId());
        response.setTournamentName(reg.getTournament().getName());
        response.setTeamId(reg.getTeam().getId());
        response.setTeamName(reg.getTeam().getName());
        response.setSquadSize(members.size());
        response.setMaxSquadSize(15);

        List<TournamentSquadMemberResponse> memberResponses = members.stream()
                .map(squadMapper::toResponse)
                .toList();
        response.setMembers(memberResponses);

        return response;
    }
}
