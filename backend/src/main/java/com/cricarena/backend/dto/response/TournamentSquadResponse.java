package com.cricarena.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class TournamentSquadResponse {

    private Long registrationId;
    private Long tournamentId;
    private String tournamentName;
    private Long teamId;
    private String teamName;
    private int squadSize;
    private int maxSquadSize = 15;
    private List<TournamentSquadMemberResponse> members = new ArrayList<>();

    public TournamentSquadResponse() {
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
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

    public int getSquadSize() {
        return squadSize;
    }

    public void setSquadSize(int squadSize) {
        this.squadSize = squadSize;
    }

    public int getMaxSquadSize() {
        return maxSquadSize;
    }

    public void setMaxSquadSize(int maxSquadSize) {
        this.maxSquadSize = maxSquadSize;
    }

    public List<TournamentSquadMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<TournamentSquadMemberResponse> members) {
        this.members = members;
    }
}
