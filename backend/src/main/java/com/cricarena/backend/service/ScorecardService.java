package com.cricarena.backend.service;

import com.cricarena.backend.dto.response.ScorecardResponse;

public interface ScorecardService {

    ScorecardResponse getScorecard(Long matchId);
}
