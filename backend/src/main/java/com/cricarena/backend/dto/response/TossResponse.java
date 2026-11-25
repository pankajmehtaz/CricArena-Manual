package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.TossDecision;

public class TossResponse {

    private Long matchId;
    private Long tossWinnerTeamId;
    private String tossWinnerTeamName;
    private TossDecision decision;
    private Long battingFirstTeamId;
    private String battingFirstTeamName;
    private Long bowlingFirstTeamId;
    private String bowlingFirstTeamName;

    public TossResponse() {
    }

    public TossResponse(Long matchId, Long tossWinnerTeamId, String tossWinnerTeamName,
                        TossDecision decision, Long battingFirstTeamId, String battingFirstTeamName,
                        Long bowlingFirstTeamId, String bowlingFirstTeamName) {
        this.matchId = matchId;
        this.tossWinnerTeamId = tossWinnerTeamId;
        this.tossWinnerTeamName = tossWinnerTeamName;
        this.decision = decision;
        this.battingFirstTeamId = battingFirstTeamId;
        this.battingFirstTeamName = battingFirstTeamName;
        this.bowlingFirstTeamId = bowlingFirstTeamId;
        this.bowlingFirstTeamName = bowlingFirstTeamName;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getTossWinnerTeamId() {
        return tossWinnerTeamId;
    }

    public void setTossWinnerTeamId(Long tossWinnerTeamId) {
        this.tossWinnerTeamId = tossWinnerTeamId;
    }

    public String getTossWinnerTeamName() {
        return tossWinnerTeamName;
    }

    public void setTossWinnerTeamName(String tossWinnerTeamName) {
        this.tossWinnerTeamName = tossWinnerTeamName;
    }

    public TossDecision getDecision() {
        return decision;
    }

    public void setDecision(TossDecision decision) {
        this.decision = decision;
    }

    public Long getBattingFirstTeamId() {
        return battingFirstTeamId;
    }

    public void setBattingFirstTeamId(Long battingFirstTeamId) {
        this.battingFirstTeamId = battingFirstTeamId;
    }

    public String getBattingFirstTeamName() {
        return battingFirstTeamName;
    }

    public void setBattingFirstTeamName(String battingFirstTeamName) {
        this.battingFirstTeamName = battingFirstTeamName;
    }

    public Long getBowlingFirstTeamId() {
        return bowlingFirstTeamId;
    }

    public void setBowlingFirstTeamId(Long bowlingFirstTeamId) {
        this.bowlingFirstTeamId = bowlingFirstTeamId;
    }

    public String getBowlingFirstTeamName() {
        return bowlingFirstTeamName;
    }

    public void setBowlingFirstTeamName(String bowlingFirstTeamName) {
        this.bowlingFirstTeamName = bowlingFirstTeamName;
    }
}
