package com.cricarena.backend.service.impl;

import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.response.PlayerResponse;
import com.cricarena.backend.entity.Player;
import com.cricarena.backend.entity.Team;
import com.cricarena.backend.exception.DuplicateResourceException;
import com.cricarena.backend.exception.ResourceNotFoundException;
import com.cricarena.backend.mapper.PlayerMapper;
import com.cricarena.backend.repository.PlayerRepository;
import com.cricarena.backend.repository.TeamRepository;
import com.cricarena.backend.service.PlayerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PlayerMapper playerMapper;

    public PlayerServiceImpl(PlayerRepository playerRepository,
                             TeamRepository teamRepository,
                             PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.playerMapper = playerMapper;
    }

    @Override
    public PlayerResponse createPlayer(PlayerRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + request.getTeamId()));

        if (!team.isActive()) {
            throw new ResourceNotFoundException(
                    "Active team not found with id: " + request.getTeamId());
        }

        if (request.getJerseyNumber() != null &&
                playerRepository.existsByJerseyNumberAndTeam_IdAndActiveTrue(
                        request.getJerseyNumber(), request.getTeamId())) {
            throw new DuplicateResourceException(
                    "Active player already exists with jersey number " + request.getJerseyNumber() +
                            " in team id: " + request.getTeamId());
        }

        Player player = playerMapper.toEntity(request, team);
        Player savedPlayer = playerRepository.save(player);
        return playerMapper.toResponse(savedPlayer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> getActivePlayers() {
        return playerRepository.findAllByActiveTrue()
                .stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        return playerMapper.toResponse(player);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> getPlayersByTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        return playerRepository.findAllByTeam_Id(teamId)
                .stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerResponse> getActivePlayersByTeam(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }

        return playerRepository.findAllByTeam_IdAndActiveTrue(teamId)
                .stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    @Override
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Team not found with id: " + request.getTeamId()));

        if (!team.isActive()) {
            throw new ResourceNotFoundException(
                    "Active team not found with id: " + request.getTeamId());
        }

        if (request.getJerseyNumber() != null &&
                playerRepository.existsByJerseyNumberAndTeam_IdAndActiveTrueAndIdNot(
                        request.getJerseyNumber(), request.getTeamId(), id)) {
            throw new DuplicateResourceException(
                    "Active player already exists with jersey number " + request.getJerseyNumber() +
                            " in team id: " + request.getTeamId());
        }

        playerMapper.updateEntity(existingPlayer, request, team);
        Player updatedPlayer = playerRepository.saveAndFlush(existingPlayer);
        return playerMapper.toResponse(updatedPlayer);
    }

    @Override
    public void deactivatePlayer(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        player.setActive(false);
        playerRepository.save(player);
    }
}
