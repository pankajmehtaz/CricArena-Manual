package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.MatchRequest;
import com.cricarena.backend.dto.response.MatchResponse;

import java.util.List;

public interface MatchService {

    MatchResponse createMatch(MatchRequest request);

    MatchResponse getMatchById(Long id);

    List<MatchResponse> getAllMatches();

    List<MatchResponse> getMatchesByTournament(Long tournamentId);

    MatchResponse updateMatch(Long id, MatchRequest request);

    void deleteMatch(Long id);
}
