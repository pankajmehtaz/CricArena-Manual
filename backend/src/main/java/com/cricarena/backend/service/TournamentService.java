package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.TournamentRequest;
import com.cricarena.backend.dto.response.TournamentResponse;

import java.util.List;

public interface TournamentService {

    TournamentResponse createTournament(TournamentRequest request);

    TournamentResponse getTournamentById(Long id);

    List<TournamentResponse> getAllTournaments();

    TournamentResponse updateTournament(Long id, TournamentRequest request);

    TournamentResponse publishTournament(Long id);

    void deleteTournament(Long id);
}
