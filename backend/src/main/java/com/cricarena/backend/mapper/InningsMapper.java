package com.cricarena.backend.mapper;

import com.cricarena.backend.dto.response.FallOfWicketResponse;
import com.cricarena.backend.dto.response.InningsResponse;
import com.cricarena.backend.entity.MatchInnings;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InningsMapper {

    public InningsResponse toResponse(MatchInnings innings) {
        return toResponse(innings, List.of());
    }

    public InningsResponse toResponse(MatchInnings innings, List<FallOfWicketResponse> fow) {
        if (innings == null) return null;

        InningsResponse response = new InningsResponse();
        response.setId(innings.getId());
        response.setMatchId(innings.getMatch().getId());
        response.setInningsNumber(innings.getInningsNumber());
        response.setBattingTeamId(innings.getBattingTeam().getId());
        response.setBattingTeamName(innings.getBattingTeam().getName());
        response.setBowlingTeamId(innings.getBowlingTeam().getId());
        response.setBowlingTeamName(innings.getBowlingTeam().getName());
        response.setTotalRuns(innings.getTotalRuns());
        response.setWickets(innings.getWickets());
        response.setLegalBalls(innings.getLegalBalls());

        int balls = innings.getLegalBalls() != null ? innings.getLegalBalls() : 0;
        response.setOvers((balls / 6) + "." + (balls % 6));

        response.setMaxOvers(innings.getMaxOvers());
        response.setTargetRuns(innings.getTargetRuns());

        if (innings.getCurrentStriker() != null) {
            response.setCurrentStrikerId(innings.getCurrentStriker().getId());
            response.setCurrentStrikerName(innings.getCurrentStriker().getFullName());
        }
        if (innings.getCurrentNonStriker() != null) {
            response.setCurrentNonStrikerId(innings.getCurrentNonStriker().getId());
            response.setCurrentNonStrikerName(innings.getCurrentNonStriker().getFullName());
        }
        if (innings.getCurrentBowler() != null) {
            response.setCurrentBowlerId(innings.getCurrentBowler().getId());
            response.setCurrentBowlerName(innings.getCurrentBowler().getFullName());
        }

        response.setStatus(innings.getStatus());
        response.setFallOfWickets(fow);
        response.setCreatedAt(innings.getCreatedAt());
        response.setUpdatedAt(innings.getUpdatedAt());

        return response;
    }
}
