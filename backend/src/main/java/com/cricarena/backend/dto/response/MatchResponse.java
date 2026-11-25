package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchResultType;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TossDecision;

import java.time.LocalDateTime;

public class MatchResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long teamAId;
    private String teamAName;
    private Long teamBId;
    private String teamBName;
    private LocalDateTime matchDateTime;
    private String venue;
    private MatchFormat format;
    private Integer maxOvers;
    private MatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long tossWinnerTeamId;
    private String tossWinnerTeamName;
    private TossDecision tossDecision;
    private Long battingFirstTeamId;
    private String battingFirstTeamName;
    private Long bowlingFirstTeamId;
    private String bowlingFirstTeamName;

    private MatchResultType resultType;
    private Long winnerTeamId;
    private String winnerTeamName;
    private Integer resultMargin;
    private String resultSummary;

    public MatchResponse() {
    }

    public MatchResponse(Long id, Long tournamentId, String tournamentName,
                         Long teamAId, String teamAName, Long teamBId, String teamBName,
                         LocalDateTime matchDateTime, String venue, MatchFormat format,
                         MatchStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.teamAId = teamAId;
        this.teamAName = teamAName;
        this.teamBId = teamBId;
        this.teamBName = teamBName;
        this.matchDateTime = matchDateTime;
        this.venue = venue;
        this.format = format;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public Long getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(Long teamAId) {
        this.teamAId = teamAId;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String teamAName) {
        this.teamAName = teamAName;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(Long teamBId) {
        this.teamBId = teamBId;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String teamBName) {
        this.teamBName = teamBName;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public void setFormat(MatchFormat format) {
        this.format = format;
    }

    public Integer getMaxOvers() {
        return maxOvers;
    }

    public void setMaxOvers(Integer maxOvers) {
        this.maxOvers = maxOvers;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
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

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
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
