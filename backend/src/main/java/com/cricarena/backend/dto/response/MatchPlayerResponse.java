package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.PlayingRole;

import java.time.LocalDateTime;

public class MatchPlayerResponse {

    private Long id;
    private Long matchId;
    private Long teamId;
    private String teamName;
    private Long playerId;
    private String playerName;
    private Integer jerseyNumber;
    private PlayingRole playingRole;
    private boolean captain;
    private boolean wicketkeeper;
    private LocalDateTime selectedAt;

    public MatchPlayerResponse() {
    }

    public MatchPlayerResponse(Long id, Long matchId, Long teamId, String teamName,
                               Long playerId, String playerName, Integer jerseyNumber,
                               PlayingRole playingRole, boolean captain, boolean wicketkeeper,
                               LocalDateTime selectedAt) {
        this.id = id;
        this.matchId = matchId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.playerId = playerId;
        this.playerName = playerName;
        this.jerseyNumber = jerseyNumber;
        this.playingRole = playingRole;
        this.captain = captain;
        this.wicketkeeper = wicketkeeper;
        this.selectedAt = selectedAt;
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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public PlayingRole getPlayingRole() {
        return playingRole;
    }

    public void setPlayingRole(PlayingRole playingRole) {
        this.playingRole = playingRole;
    }

    public boolean isCaptain() {
        return captain;
    }

    public void setCaptain(boolean captain) {
        this.captain = captain;
    }

    public boolean isWicketkeeper() {
        return wicketkeeper;
    }

    public void setWicketkeeper(boolean wicketkeeper) {
        this.wicketkeeper = wicketkeeper;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }

    public void setSelectedAt(LocalDateTime selectedAt) {
        this.selectedAt = selectedAt;
    }
}
