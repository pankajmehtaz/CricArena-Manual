package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class StartInningsRequest {

    @NotNull(message = "Innings number is required")
    private Integer inningsNumber;

    @NotNull(message = "Striker player ID is required")
    private Long strikerPlayerId;

    @NotNull(message = "Non-striker player ID is required")
    private Long nonStrikerPlayerId;

    @NotNull(message = "Bowler player ID is required")
    private Long bowlerPlayerId;

    public StartInningsRequest() {
    }

    public StartInningsRequest(Integer inningsNumber, Long strikerPlayerId, Long nonStrikerPlayerId, Long bowlerPlayerId) {
        this.inningsNumber = inningsNumber;
        this.strikerPlayerId = strikerPlayerId;
        this.nonStrikerPlayerId = nonStrikerPlayerId;
        this.bowlerPlayerId = bowlerPlayerId;
    }

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Long getStrikerPlayerId() {
        return strikerPlayerId;
    }

    public void setStrikerPlayerId(Long strikerPlayerId) {
        this.strikerPlayerId = strikerPlayerId;
    }

    public Long getNonStrikerPlayerId() {
        return nonStrikerPlayerId;
    }

    public void setNonStrikerPlayerId(Long nonStrikerPlayerId) {
        this.nonStrikerPlayerId = nonStrikerPlayerId;
    }

    public Long getBowlerPlayerId() {
        return bowlerPlayerId;
    }

    public void setBowlerPlayerId(Long bowlerPlayerId) {
        this.bowlerPlayerId = bowlerPlayerId;
    }
}
