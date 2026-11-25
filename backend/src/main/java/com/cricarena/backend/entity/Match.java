package com.cricarena.backend.entity;

import com.cricarena.backend.entity.enums.MatchFormat;
import com.cricarena.backend.entity.enums.MatchResultType;
import com.cricarena.backend.entity.enums.MatchStatus;
import com.cricarena.backend.entity.enums.TossDecision;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_a_id", nullable = false)
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_b_id", nullable = false)
    private Team teamB;

    @Column(name = "match_date_time", nullable = false)
    private LocalDateTime matchDateTime;

    @Column(name = "venue", nullable = false, length = 150)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 30)
    private MatchFormat format;

    @Column(name = "max_overs", nullable = false)
    private Integer maxOvers = 20;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toss_winner_team_id")
    private Team tossWinnerTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_decision", length = 10)
    private TossDecision tossDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batting_first_team_id")
    private Team battingFirstTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowling_first_team_id")
    private Team bowlingFirstTeam;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", length = 30)
    private MatchResultType resultType;

    @Column(name = "result_margin")
    private Integer resultMargin;

    @Column(name = "result_summary", length = 255)
    private String resultSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_team_id")
    private Team winnerTeam;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Match() {
    }

    public Match(Tournament tournament, Team teamA, Team teamB, LocalDateTime matchDateTime,
                 String venue, MatchFormat format) {
        this(tournament, teamA, teamB, matchDateTime, venue, format, null, MatchStatus.SCHEDULED);
    }

    public Match(Tournament tournament, Team teamA, Team teamB, LocalDateTime matchDateTime,
                 String venue, MatchFormat format, MatchStatus status) {
        this(tournament, teamA, teamB, matchDateTime, venue, format, null, status);
    }

    public Match(Tournament tournament, Team teamA, Team teamB, LocalDateTime matchDateTime,
                 String venue, MatchFormat format, Integer maxOvers, MatchStatus status) {
        this.tournament = tournament;
        this.teamA = teamA;
        this.teamB = teamB;
        this.matchDateTime = matchDateTime;
        this.venue = venue;
        this.format = format;
        this.maxOvers = resolveMaxOvers(format, maxOvers);
        this.status = status != null ? status : MatchStatus.SCHEDULED;
    }

    public static int resolveMaxOvers(MatchFormat format, Integer customMaxOvers) {
        if (format == MatchFormat.T20) {
            return (customMaxOvers != null && customMaxOvers > 0) ? customMaxOvers : 20;
        } else if (format == MatchFormat.ODI) {
            return (customMaxOvers != null && customMaxOvers > 0) ? customMaxOvers : 50;
        } else if (format == MatchFormat.CUSTOM) {
            if (customMaxOvers == null || customMaxOvers <= 0) {
                throw new IllegalArgumentException("CUSTOM match format requires a positive maxOvers value");
            }
            return customMaxOvers;
        } else if (format == MatchFormat.TEST) {
            throw new IllegalArgumentException("TEST match scoring is not supported in this two-innings limited-overs MVP");
        }
        return 20;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = MatchStatus.SCHEDULED;
        }
        if (this.maxOvers == null) {
            this.maxOvers = resolveMaxOvers(this.format, null);
        }
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

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Team getTeamA() {
        return teamA;
    }

    public void setTeamA(Team teamA) {
        this.teamA = teamA;
    }

    public Team getTeamB() {
        return teamB;
    }

    public void setTeamB(Team teamB) {
        this.teamB = teamB;
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

    public Team getTossWinnerTeam() {
        return tossWinnerTeam;
    }

    public void setTossWinnerTeam(Team tossWinnerTeam) {
        this.tossWinnerTeam = tossWinnerTeam;
    }

    public TossDecision getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(TossDecision tossDecision) {
        this.tossDecision = tossDecision;
    }

    public Team getBattingFirstTeam() {
        return battingFirstTeam;
    }

    public void setBattingFirstTeam(Team battingFirstTeam) {
        this.battingFirstTeam = battingFirstTeam;
    }

    public Team getBowlingFirstTeam() {
        return bowlingFirstTeam;
    }

    public void setBowlingFirstTeam(Team bowlingFirstTeam) {
        this.bowlingFirstTeam = bowlingFirstTeam;
    }

    public Integer getMaxOvers() {
        return maxOvers;
    }

    public void setMaxOvers(Integer maxOvers) {
        this.maxOvers = maxOvers;
    }

    public MatchResultType getResultType() {
        return resultType;
    }

    public void setResultType(MatchResultType resultType) {
        this.resultType = resultType;
    }

    public Integer getResultMargin() {
        return resultMargin;
    }

    public void setResultMargin(Integer resultMargin) {
        this.resultMargin = resultMargin;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    public Team getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }
}
