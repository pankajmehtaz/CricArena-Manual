package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.StartInningsRequest;
import com.cricarena.backend.dto.response.InningsResponse;

import java.util.List;

public interface InningsService {

    InningsResponse startInnings(Long matchId, StartInningsRequest request);

    List<InningsResponse> getInningsByMatch(Long matchId);

    InningsResponse getInningsByMatchAndNumber(Long matchId, Integer inningsNumber);
}
