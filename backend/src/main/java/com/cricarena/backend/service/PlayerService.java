package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.PlayerRequest;
import com.cricarena.backend.dto.response.PlayerResponse;

import java.util.List;

public interface PlayerService {

    PlayerResponse createPlayer(PlayerRequest request);

    List<PlayerResponse> getAllPlayers();

    List<PlayerResponse> getActivePlayers();

    PlayerResponse getPlayerById(Long id);

    List<PlayerResponse> getPlayersByTeam(Long teamId);

    List<PlayerResponse> getActivePlayersByTeam(Long teamId);

    PlayerResponse updatePlayer(Long id, PlayerRequest request);

    void deactivatePlayer(Long id);
}
