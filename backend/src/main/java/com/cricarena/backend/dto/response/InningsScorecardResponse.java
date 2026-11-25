package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.InningsStatus;

import java.util.List;

public class InningsScorecardResponse {

    private Long id;
    private Long matchId;
    private Integer inningsNumber;
    private Long battingTeamId;
    private String battingTeamName;
    private Long bowlingTeamId;
    private String bowlingTeamName;
    private Integer totalRuns;
    private Integer wickets;
    private Integer legalBalls;
    private String overs;
    private Integer maxOvers;
    private Integer targetRuns;
    private Double runRate;
    private InningsStatus status;

    private List<BatterScorecardEntry> batters;
    private List<BowlerScorecardEntry> bowlers;
    private ExtrasResponse extras;
    private List<FallOfWicketResponse> fallOfWickets;
    private List<DeliveryResponse> recentDeliveries;

    public InningsScorecardResponse() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Integer getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }

    public Long getBattingTeamId() { return battingTeamId; }
    public void setBattingTeamId(Long battingTeamId) { this.battingTeamId = battingTeamId; }

    public String getBattingTeamName() { return battingTeamName; }
    public void setBattingTeamName(String battingTeamName) { this.battingTeamName = battingTeamName; }

    public Long getBowlingTeamId() { return bowlingTeamId; }
    public void setBowlingTeamId(Long bowlingTeamId) { this.bowlingTeamId = bowlingTeamId; }

    public String getBowlingTeamName() { return bowlingTeamName; }
    public void setBowlingTeamName(String bowlingTeamName) { this.bowlingTeamName = bowlingTeamName; }

    public Integer getTotalRuns() { return totalRuns; }
    public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }

    public Integer getWickets() { return wickets; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }

    public Integer getLegalBalls() { return legalBalls; }
    public void setLegalBalls(Integer legalBalls) { this.legalBalls = legalBalls; }

    public String getOvers() { return overs; }
    public void setOvers(String overs) { this.overs = overs; }

    public Integer getMaxOvers() { return maxOvers; }
    public void setMaxOvers(Integer maxOvers) { this.maxOvers = maxOvers; }

    public Integer getTargetRuns() { return targetRuns; }
    public void setTargetRuns(Integer targetRuns) { this.targetRuns = targetRuns; }

    public Double getRunRate() { return runRate; }
    public void setRunRate(Double runRate) { this.runRate = runRate; }

    public InningsStatus getStatus() { return status; }
    public void setStatus(InningsStatus status) { this.status = status; }

    public List<BatterScorecardEntry> getBatters() { return batters; }
    public void setBatters(List<BatterScorecardEntry> batters) { this.batters = batters; }

    public List<BowlerScorecardEntry> getBowlers() { return bowlers; }
    public void setBowlers(List<BowlerScorecardEntry> bowlers) { this.bowlers = bowlers; }

    public ExtrasResponse getExtras() { return extras; }
    public void setExtras(ExtrasResponse extras) { this.extras = extras; }

    public List<FallOfWicketResponse> getFallOfWickets() { return fallOfWickets; }
    public void setFallOfWickets(List<FallOfWicketResponse> fallOfWickets) { this.fallOfWickets = fallOfWickets; }

    public List<DeliveryResponse> getRecentDeliveries() { return recentDeliveries; }
    public void setRecentDeliveries(List<DeliveryResponse> recentDeliveries) { this.recentDeliveries = recentDeliveries; }
}
