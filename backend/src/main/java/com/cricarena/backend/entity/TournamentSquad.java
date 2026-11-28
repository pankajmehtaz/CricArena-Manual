package com.cricarena.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "tournament_squads",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_ts_registration_player", columnNames = {"tournament_registration_id", "player_id"}),
        @UniqueConstraint(name = "uk_ts_tournament_player", columnNames = {"tournament_id", "player_id"})
    }
)
public class TournamentSquad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_registration_id", nullable = false)
    private TournamentRegistration tournamentRegistration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(name = "playing_role", nullable = false, length = 30)
    private String playingRole;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    public TournamentSquad() {
    }

    public TournamentSquad(TournamentRegistration tournamentRegistration,
                           Tournament tournament,
                           Player player,
                           String playerName,
                           Integer jerseyNumber,
                           String playingRole) {
        this.tournamentRegistration = tournamentRegistration;
        this.tournament = tournament;
        this.player = player;
        this.playerName = playerName;
        this.jerseyNumber = jerseyNumber;
        this.playingRole = playingRole;
    }

    @PrePersist
    protected void onCreate() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TournamentRegistration getTournamentRegistration() {
        return tournamentRegistration;
    }

    public void setTournamentRegistration(TournamentRegistration tournamentRegistration) {
        this.tournamentRegistration = tournamentRegistration;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
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

    public String getPlayingRole() {
        return playingRole;
    }

    public void setPlayingRole(String playingRole) {
        this.playingRole = playingRole;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
