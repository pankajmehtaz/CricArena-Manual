package com.cricarena.backend.service;

import com.cricarena.backend.dto.request.ChangeBowlerRequest;
import com.cricarena.backend.dto.request.RecordDeliveryRequest;
import com.cricarena.backend.dto.response.DeliveryResponse;
import com.cricarena.backend.dto.response.DeliveryResultResponse;
import com.cricarena.backend.dto.response.InningsResponse;

import java.util.List;

public interface DeliveryService {

    DeliveryResultResponse recordDelivery(Long matchId, RecordDeliveryRequest request);

    List<DeliveryResponse> getDeliveriesByMatch(Long matchId);

    InningsResponse changeBowler(Long matchId, ChangeBowlerRequest request);
}
