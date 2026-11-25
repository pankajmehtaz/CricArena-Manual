package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.MatchStatus;

public class ScorecardResponse {

    private Long matchId;
    private MatchStatus matchStatus;
    private InningsScorecardResponse innings1;
    private InningsScorecardResponse innings2;
    private MatchResultResponse result;

    public ScorecardResponse() {
    }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public InningsScorecardResponse getInnings1() { return innings1; }
    public void setInnings1(InningsScorecardResponse innings1) { this.innings1 = innings1; }

    public InningsScorecardResponse getInnings2() { return innings2; }
    public void setInnings2(InningsScorecardResponse innings2) { this.innings2 = innings2; }

    public MatchResultResponse getResult() { return result; }
    public void setResult(MatchResultResponse result) { this.result = result; }
}
