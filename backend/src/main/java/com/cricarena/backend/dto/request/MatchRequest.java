package com.cricarena.backend.dto.request;

import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MatchRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Team A ID is required")
    private Long teamAId;

    @NotNull(message = "Team B ID is required")
    private Long teamBId;

    @NotNull(message = "Match date and time is required")
    private LocalDateTime matchDateTime;

    @NotBlank(message = "Venue is required")
    @Size(max = 150, message = "Venue must not exceed 150 characters")
    private String venue;

    @NotNull(message = "Match format is required")
    private MatchFormat format;

    private Integer maxOvers;

    private MatchStatus status;

    public MatchRequest() {
    }

    public MatchRequest(Long tournamentId, Long teamAId, Long teamBId,
                        LocalDateTime matchDateTime, String venue, MatchFormat format) {
        this(tournamentId, teamAId, teamBId, matchDateTime, venue, format, null, null);
    }

    public MatchRequest(Long tournamentId, Long teamAId, Long teamBId,
                        LocalDateTime matchDateTime, String venue, MatchFormat format, MatchStatus status) {
        this(tournamentId, teamAId, teamBId, matchDateTime, venue, format, null, status);
    }

    public MatchRequest(Long tournamentId, Long teamAId, Long teamBId,
                        LocalDateTime matchDateTime, String venue, MatchFormat format, Integer maxOvers, MatchStatus status) {
        this.tournamentId = tournamentId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.matchDateTime = matchDateTime;
        this.venue = venue;
        this.format = format;
        this.maxOvers = maxOvers;
        this.status = status;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(Long teamAId) {
        this.teamAId = teamAId;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(Long teamBId) {
        this.teamBId = teamBId;
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
}
