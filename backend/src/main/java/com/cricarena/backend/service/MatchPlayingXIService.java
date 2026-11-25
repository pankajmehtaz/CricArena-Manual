package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.PlayingXISelectionRequest;
import com.cricarena.backend.dto.response.MatchPlayerResponse;
import com.cricarena.backend.dto.response.MatchPlayingXIResponse;

import java.util.List;

public interface MatchPlayingXIService {

    MatchPlayingXIResponse setPlayingXI(Long matchId, PlayingXISelectionRequest request);

    MatchPlayingXIResponse getPlayingXI(Long matchId);

    List<MatchPlayerResponse> getTeamPlayingXI(Long matchId, Long teamId);
}
