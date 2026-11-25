package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class ChangeBowlerRequest {

    @NotNull(message = "Bowler player ID is required")
    private Long bowlerPlayerId;

    public ChangeBowlerRequest() {
    }

    public ChangeBowlerRequest(Long bowlerPlayerId) {
        this.bowlerPlayerId = bowlerPlayerId;
    }

    public Long getBowlerPlayerId() {
        return bowlerPlayerId;
    }

    public void setBowlerPlayerId(Long bowlerPlayerId) {
        this.bowlerPlayerId = bowlerPlayerId;
    }
}
