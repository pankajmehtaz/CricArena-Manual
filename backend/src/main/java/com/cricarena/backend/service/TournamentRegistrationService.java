package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.TournamentRegistrationResponse;

import java.util.List;

public interface TournamentRegistrationService {

    TournamentRegistrationResponse registerTeam(Long tournamentId, Long teamId);

    List<TournamentRegistrationResponse> getRegisteredTeams(Long tournamentId);

    void removeRegistration(Long tournamentId, Long teamId);
}
