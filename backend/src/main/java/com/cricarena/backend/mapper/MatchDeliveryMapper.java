package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.DeliveryResponse;
import com.cricarena.backend.entity.MatchDelivery;
import org.springframework.stereotype.Component;

@Component
public class MatchDeliveryMapper {

    public DeliveryResponse toResponse(MatchDelivery delivery) {
        if (delivery == null) return null;

        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setInningsId(delivery.getInnings().getId());
        response.setBallSequence(delivery.getBallSequence());
        response.setOverNumber(delivery.getOverNumber());
        response.setBallInOver(delivery.getBallInOver());
        response.setOverText(delivery.getOverNumber() + "." + delivery.getBallInOver());

        if (delivery.getBowler() != null) {
            response.setBowlerId(delivery.getBowler().getId());
            response.setBowlerName(delivery.getBowler().getFullName());
        }
        if (delivery.getStriker() != null) {
            response.setStrikerId(delivery.getStriker().getId());
            response.setStrikerName(delivery.getStriker().getFullName());
        }
        if (delivery.getNonStriker() != null) {
            response.setNonStrikerId(delivery.getNonStriker().getId());
            response.setNonStrikerName(delivery.getNonStriker().getFullName());
        }

        response.setBatterRuns(delivery.getBatterRuns());
        response.setExtraType(delivery.getExtraType());
        response.setExtraRuns(delivery.getExtraRuns());
        response.setTotalRuns(delivery.getTotalRuns());
        response.setLegal(delivery.isLegal());

        response.setWicket(delivery.isWicket());
        response.setDismissalType(delivery.getDismissalType());
        if (delivery.getDismissedPlayer() != null) {
            response.setDismissedPlayerId(delivery.getDismissedPlayer().getId());
            response.setDismissedPlayerName(delivery.getDismissedPlayer().getFullName());
        }
        if (delivery.getFielder() != null) {
            response.setFielderId(delivery.getFielder().getId());
            response.setFielderName(delivery.getFielder().getFullName());
        }
        if (delivery.getNextBatter() != null) {
            response.setNextBatterId(delivery.getNextBatter().getId());
            response.setNextBatterName(delivery.getNextBatter().getFullName());
        }

        response.setCreatedAt(delivery.getCreatedAt());

        return response;
    }
}
