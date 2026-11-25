package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.MatchResultType;

public class MatchResultResponse {

    private MatchResultType resultType;
    private Long winnerTeamId;
    private String winnerTeamName;
    private Integer resultMargin;
    private String resultSummary;

    public MatchResultResponse() {
    }

    public MatchResultType getResultType() { return resultType; }
    public void setResultType(MatchResultType resultType) { this.resultType = resultType; }

    public Long getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Long winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public String getWinnerTeamName() { return winnerTeamName; }
    public void setWinnerTeamName(String winnerTeamName) { this.winnerTeamName = winnerTeamName; }

    public Integer getResultMargin() { return resultMargin; }
    public void setResultMargin(Integer resultMargin) { this.resultMargin = resultMargin; }

    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
}
