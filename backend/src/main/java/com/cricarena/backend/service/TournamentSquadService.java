package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.TournamentSquadResponse;

import java.util.List;

public interface TournamentSquadService {

    TournamentSquadResponse addPlayerToSquad(Long tournamentId, Long teamId, Long playerId);

    TournamentSquadResponse setSquad(Long tournamentId, Long teamId, List<Long> playerIds);

    TournamentSquadResponse getSquad(Long tournamentId, Long teamId);

    void removePlayerFromSquad(Long tournamentId, Long teamId, Long playerId);
}
