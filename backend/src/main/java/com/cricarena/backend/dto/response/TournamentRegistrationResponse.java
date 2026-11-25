package com.cricarena.backend.dto.response;

import java.time.LocalDateTime;

public class TournamentRegistrationResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long teamId;
    private String teamName;
    private LocalDateTime registeredAt;
    private TeamResponse team;

    public TournamentRegistrationResponse() {
    }

    public TournamentRegistrationResponse(Long id, Long tournamentId, String tournamentName,
                                        Long teamId, String teamName, LocalDateTime registeredAt,
                                        TeamResponse team) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.teamId = teamId;
        this.teamName = teamName;
        this.registeredAt = registeredAt;
        this.team = team;
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

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public TeamResponse getTeam() {
        return team;
    }

    public void setTeam(TeamResponse team) {
        this.team = team;
    }
}
