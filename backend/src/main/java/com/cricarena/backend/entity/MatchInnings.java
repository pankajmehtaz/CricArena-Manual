package com.cricarena.backend.entity;

import com.cricarena.backend.entity.enums.InningsStatus;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "match_innings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_match_innings_number", columnNames = {"match_id", "innings_number"})
    }
)
public class MatchInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "innings_number", nullable = false)
    private Integer inningsNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batting_team_id", nullable = false)
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    private Team bowlingTeam;

    @Column(name = "total_runs", nullable = false)
    private Integer totalRuns = 0;

    @Column(name = "wickets", nullable = false)
    private Integer wickets = 0;

    @Column(name = "legal_balls", nullable = false)
    private Integer legalBalls = 0;

    @Column(name = "max_overs", nullable = false)
    private Integer maxOvers = 20;

    @Column(name = "target_runs")
    private Integer targetRuns;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_striker_id")
    private Player currentStriker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_non_striker_id")
    private Player currentNonStriker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_bowler_id")
    private Player currentBowler;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private InningsStatus status = InningsStatus.NOT_STARTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MatchInnings() {
    }

    public MatchInnings(Match match, Integer inningsNumber, Team battingTeam, Team bowlingTeam,
                        Integer maxOvers, Player currentStriker, Player currentNonStriker, Player currentBowler) {
        this.match = match;
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.maxOvers = maxOvers != null ? maxOvers : 20;
        this.currentStriker = currentStriker;
        this.currentNonStriker = currentNonStriker;
        this.currentBowler = currentBowler;
        this.totalRuns = 0;
        this.wickets = 0;
        this.legalBalls = 0;
        this.status = InningsStatus.IN_PROGRESS;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.totalRuns == null) this.totalRuns = 0;
        if (this.wickets == null) this.wickets = 0;
        if (this.legalBalls == null) this.legalBalls = 0;
        if (this.status == null) this.status = InningsStatus.NOT_STARTED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(Team bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
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

    public Player getCurrentStriker() {
        return currentStriker;
    }

    public void setCurrentStriker(Player currentStriker) {
        this.currentStriker = currentStriker;
    }

    public Player getCurrentNonStriker() {
        return currentNonStriker;
    }

    public void setCurrentNonStriker(Player currentNonStriker) {
        this.currentNonStriker = currentNonStriker;
    }

    public Player getCurrentBowler() {
        return currentBowler;
    }

    public void setCurrentBowler(Player currentBowler) {
        this.currentBowler = currentBowler;
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
}
