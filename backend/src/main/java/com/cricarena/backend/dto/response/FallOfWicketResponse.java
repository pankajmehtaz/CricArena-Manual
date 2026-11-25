package com.cricarena.backend.dto.response;

public class FallOfWicketResponse {

    private Integer wicketNumber;
    private Integer score;
    private String overs;
    private Long dismissedPlayerId;
    private String dismissedPlayerName;

    public FallOfWicketResponse() {
    }

    public FallOfWicketResponse(Integer wicketNumber, Integer score, String overs,
                                Long dismissedPlayerId, String dismissedPlayerName) {
        this.wicketNumber = wicketNumber;
        this.score = score;
        this.overs = overs;
        this.dismissedPlayerId = dismissedPlayerId;
        this.dismissedPlayerName = dismissedPlayerName;
    }

    public Integer getWicketNumber() {
        return wicketNumber;
    }

    public void setWicketNumber(Integer wicketNumber) {
        this.wicketNumber = wicketNumber;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getOvers() {
        return overs;
    }

    public void setOvers(String overs) {
        this.overs = overs;
    }

    public Long getDismissedPlayerId() {
        return dismissedPlayerId;
    }

    public void setDismissedPlayerId(Long dismissedPlayerId) {
        this.dismissedPlayerId = dismissedPlayerId;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(String dismissedPlayerName) {
        this.dismissedPlayerName = dismissedPlayerName;
    }
}
