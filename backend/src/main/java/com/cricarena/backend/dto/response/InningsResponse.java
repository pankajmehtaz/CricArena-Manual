package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.InningsStatus;

import java.time.LocalDateTime;

public class InningsResponse {

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
    private String overs; // e.g. "0.0", "1.3"
    private Integer maxOvers;
    private Integer targetRuns;
    private Long currentStrikerId;
    private String currentStrikerName;
    private Long currentNonStrikerId;
    private String currentNonStrikerName;
    private Long currentBowlerId;
    private String currentBowlerName;
    private InningsStatus status;
    private java.util.List<FallOfWicketResponse> fallOfWickets = new java.util.ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InningsResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Long getBattingTeamId() {
        return battingTeamId;
    }

    public void setBattingTeamId(Long battingTeamId) {
        this.battingTeamId = battingTeamId;
    }

    public String getBattingTeamName() {
        return battingTeamName;
    }

    public void setBattingTeamName(String battingTeamName) {
        this.battingTeamName = battingTeamName;
    }

    public Long getBowlingTeamId() {
        return bowlingTeamId;
    }

    public void setBowlingTeamId(Long bowlingTeamId) {
        this.bowlingTeamId = bowlingTeamId;
    }

    public String getBowlingTeamName() {
        return bowlingTeamName;
    }

    public void setBowlingTeamName(String bowlingTeamName) {
        this.bowlingTeamName = bowlingTeamName;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public Integer getWickets() {
        return wickets;
    }

    public void setWickets(Integer wickets) {
        this.wickets = wickets;
    }

    public Integer getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(Integer legalBalls) {
        this.legalBalls = legalBalls;
    }

    public String getOvers() {
        return overs;
    }

    public void setOvers(String overs) {
        this.overs = overs;
    }

    public Integer getMaxOvers() {
        return maxOvers;
    }

    public void setMaxOvers(Integer maxOvers) {
        this.maxOvers = maxOvers;
    }

    public Integer getTargetRuns() {
        return targetRuns;
    }

    public void setTargetRuns(Integer targetRuns) {
        this.targetRuns = targetRuns;
    }

    public Long getCurrentStrikerId() {
        return currentStrikerId;
    }

    public void setCurrentStrikerId(Long currentStrikerId) {
        this.currentStrikerId = currentStrikerId;
    }

    public String getCurrentStrikerName() {
        return currentStrikerName;
    }

    public void setCurrentStrikerName(String currentStrikerName) {
        this.currentStrikerName = currentStrikerName;
    }

    public Long getCurrentNonStrikerId() {
        return currentNonStrikerId;
    }

    public void setCurrentNonStrikerId(Long currentNonStrikerId) {
        this.currentNonStrikerId = currentNonStrikerId;
    }

    public String getCurrentNonStrikerName() {
        return currentNonStrikerName;
    }

    public void setCurrentNonStrikerName(String currentNonStrikerName) {
        this.currentNonStrikerName = currentNonStrikerName;
    }

    public Long getCurrentBowlerId() {
        return currentBowlerId;
    }

    public void setCurrentBowlerId(Long currentBowlerId) {
        this.currentBowlerId = currentBowlerId;
    }

    public String getCurrentBowlerName() {
        return currentBowlerName;
    }

    public void setCurrentBowlerName(String currentBowlerName) {
        this.currentBowlerName = currentBowlerName;
    }

    public InningsStatus getStatus() {
        return status;
    }

    public void setStatus(InningsStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.util.List<FallOfWicketResponse> getFallOfWickets() {
        return fallOfWickets;
    }

    public void setFallOfWickets(java.util.List<FallOfWicketResponse> fallOfWickets) {
        this.fallOfWickets = fallOfWickets != null ? fallOfWickets : new java.util.ArrayList<>();
    }
}
