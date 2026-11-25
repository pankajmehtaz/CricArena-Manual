package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.TossRequest;
import com.cricarena.backend.dto.response.TossResponse;

public interface TossService {

    TossResponse recordToss(Long matchId, TossRequest request);

    TossResponse getToss(Long matchId);
}
