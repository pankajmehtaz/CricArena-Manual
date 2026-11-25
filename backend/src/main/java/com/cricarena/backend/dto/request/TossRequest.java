package com.cricarena.backend.dto.request;

import com.cricarena.backend.entity.enums.TossDecision;
import jakarta.validation.constraints.NotNull;

public class TossRequest {

    @NotNull(message = "Toss winner team ID is required")
    private Long tossWinnerTeamId;

    @NotNull(message = "Toss decision is required")
    private TossDecision decision;

    public TossRequest() {
    }

    public TossRequest(Long tossWinnerTeamId, TossDecision decision) {
        this.tossWinnerTeamId = tossWinnerTeamId;
        this.decision = decision;
    }

    public Long getTossWinnerTeamId() {
        return tossWinnerTeamId;
    }

    public void setTossWinnerTeamId(Long tossWinnerTeamId) {
        this.tossWinnerTeamId = tossWinnerTeamId;
    }

    public TossDecision getDecision() {
        return decision;
    }

    public void setDecision(TossDecision decision) {
        this.decision = decision;
    }
}
