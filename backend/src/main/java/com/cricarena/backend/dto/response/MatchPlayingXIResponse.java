package com.cricarena.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class MatchPlayingXIResponse {

    private Long matchId;
    private Long teamAId;
    private String teamAName;
    private List<MatchPlayerResponse> teamAPlayers = new ArrayList<>();
    private Long teamBId;
    private String teamBName;
    private List<MatchPlayerResponse> teamBPlayers = new ArrayList<>();

    public MatchPlayingXIResponse() {
    }

    public MatchPlayingXIResponse(Long matchId, Long teamAId, String teamAName,
                                  List<MatchPlayerResponse> teamAPlayers,
                                  Long teamBId, String teamBName,
                                  List<MatchPlayerResponse> teamBPlayers) {
        this.matchId = matchId;
        this.teamAId = teamAId;
        this.teamAName = teamAName;
        this.teamAPlayers = teamAPlayers != null ? teamAPlayers : new ArrayList<>();
        this.teamBId = teamBId;
        this.teamBName = teamBName;
        this.teamBPlayers = teamBPlayers != null ? teamBPlayers : new ArrayList<>();
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
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

    public List<MatchPlayerResponse> getTeamAPlayers() {
        return teamAPlayers;
    }

    public void setTeamAPlayers(List<MatchPlayerResponse> teamAPlayers) {
        this.teamAPlayers = teamAPlayers;
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

    public List<MatchPlayerResponse> getTeamBPlayers() {
        return teamBPlayers;
    }

    public void setTeamBPlayers(List<MatchPlayerResponse> teamBPlayers) {
        this.teamBPlayers = teamBPlayers;
    }
}
