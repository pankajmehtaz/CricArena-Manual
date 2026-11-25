package com.cricarena.backend.entity;

import com.cricarena.backend.entity.enums.PlayingRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "match_playing_xi",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_player", columnNames = {"match_id", "player_id"})
    }
)
public class MatchPlayingXI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "playing_role", nullable = false, length = 30)
    private PlayingRole playingRole;

    @Column(name = "is_captain", nullable = false)
    private boolean captain = false;

    @Column(name = "is_wicketkeeper", nullable = false)
    private boolean wicketkeeper = false;

    @Column(name = "selected_at", nullable = false, updatable = false)
    private LocalDateTime selectedAt;

    public MatchPlayingXI() {
    }

    public MatchPlayingXI(Match match, Team team, Player player, String playerName,
                          Integer jerseyNumber, PlayingRole playingRole,
                          boolean captain, boolean wicketkeeper) {
        this.match = match;
        this.team = team;
        this.player = player;
        this.playerName = playerName;
        this.jerseyNumber = jerseyNumber;
        this.playingRole = playingRole;
        this.captain = captain;
        this.wicketkeeper = wicketkeeper;
    }

    @PrePersist
    protected void onCreate() {
        if (this.selectedAt == null) {
            this.selectedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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
