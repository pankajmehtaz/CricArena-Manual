package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SquadBatchRequest {

    @NotEmpty(message = "Player IDs list cannot be empty")
    @Size(max = 15, message = "Tournament squad cannot exceed 15 players")
    private List<Long> playerIds;

    public SquadBatchRequest() {
    }

    public SquadBatchRequest(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }
}
