package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class SquadMemberRequest {

    @NotNull(message = "Player ID is required")
    private Long playerId;

    public SquadMemberRequest() {
    }

    public SquadMemberRequest(Long playerId) {
        this.playerId = playerId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}
