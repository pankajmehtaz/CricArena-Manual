package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PlayingXISelectionRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Player IDs list is required")
    @Size(min = 11, max = 11, message = "Playing XI must contain exactly 11 players")
    private List<Long> playerIds;

    @NotNull(message = "Captain player ID is required")
    private Long captainPlayerId;

    @NotNull(message = "Wicketkeeper player ID is required")
    private Long wicketkeeperPlayerId;

    public PlayingXISelectionRequest() {
    }

    public PlayingXISelectionRequest(Long teamId, List<Long> playerIds,
                                     Long captainPlayerId, Long wicketkeeperPlayerId) {
        this.teamId = teamId;
        this.playerIds = playerIds;
        this.captainPlayerId = captainPlayerId;
        this.wicketkeeperPlayerId = wicketkeeperPlayerId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public Long getCaptainPlayerId() {
        return captainPlayerId;
    }

    public void setCaptainPlayerId(Long captainPlayerId) {
        this.captainPlayerId = captainPlayerId;
    }

    public Long getWicketkeeperPlayerId() {
        return wicketkeeperPlayerId;
    }

    public void setWicketkeeperPlayerId(Long wicketkeeperPlayerId) {
        this.wicketkeeperPlayerId = wicketkeeperPlayerId;
    }
}
