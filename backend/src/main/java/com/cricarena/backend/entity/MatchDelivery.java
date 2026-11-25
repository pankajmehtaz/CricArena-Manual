package com.cricarena.backend.entity;

import com.cricarena.backend.entity.enums.DismissalType;
import com.cricarena.backend.entity.enums.ExtraType;
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
    name = "match_deliveries",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_innings_ball_sequence", columnNames = {"innings_id", "ball_sequence"})
    }
)
public class MatchDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private MatchInnings innings;

    @Column(name = "ball_sequence", nullable = false)
    private Integer ballSequence;

    @Column(name = "over_number", nullable = false)
    private Integer overNumber;

    @Column(name = "ball_in_over", nullable = false)
    private Integer ballInOver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowler_id", nullable = false)
    private Player bowler;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "striker_id", nullable = false)
    private Player striker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "non_striker_id", nullable = false)
    private Player nonStriker;

    @Column(name = "batter_runs", nullable = false)
    private Integer batterRuns = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_type", nullable = false, length = 20)
    private ExtraType extraType = ExtraType.NONE;

    @Column(name = "extra_runs", nullable = false)
    private Integer extraRuns = 0;

    @Column(name = "total_runs", nullable = false)
    private Integer totalRuns = 0;

    @Column(name = "is_legal", nullable = false)
    private boolean isLegal = true;

    @Column(name = "is_wicket", nullable = false)
    private boolean isWicket = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "dismissal_type", length = 30)
    private DismissalType dismissalType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_player_id")
    private Player dismissedPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fielder_id")
    private Player fielder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_batter_id")
    private Player nextBatter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MatchDelivery() {
    }

    public MatchDelivery(MatchInnings innings, Integer ballSequence, Integer overNumber, Integer ballInOver,
                         Player bowler, Player striker, Player nonStriker,
                         Integer batterRuns, ExtraType extraType, Integer extraRuns,
                         Integer totalRuns, boolean isLegal) {
        this(innings, ballSequence, overNumber, ballInOver, bowler, striker, nonStriker,
                batterRuns, extraType, extraRuns, totalRuns, isLegal, false, null, null, null, null);
    }

    public MatchDelivery(MatchInnings innings, Integer ballSequence, Integer overNumber, Integer ballInOver,
                         Player bowler, Player striker, Player nonStriker,
                         Integer batterRuns, ExtraType extraType, Integer extraRuns,
                         Integer totalRuns, boolean isLegal,
                         boolean isWicket, DismissalType dismissalType,
                         Player dismissedPlayer, Player fielder, Player nextBatter) {
        this.innings = innings;
        this.ballSequence = ballSequence;
        this.overNumber = overNumber;
        this.ballInOver = ballInOver;
        this.bowler = bowler;
        this.striker = striker;
        this.nonStriker = nonStriker;
        this.batterRuns = batterRuns != null ? batterRuns : 0;
        this.extraType = extraType != null ? extraType : ExtraType.NONE;
        this.extraRuns = extraRuns != null ? extraRuns : 0;
        this.totalRuns = totalRuns != null ? totalRuns : 0;
        this.isLegal = isLegal;
        this.isWicket = isWicket;
        this.dismissalType = dismissalType;
        this.dismissedPlayer = dismissedPlayer;
        this.fielder = fielder;
        this.nextBatter = nextBatter;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.batterRuns == null) this.batterRuns = 0;
        if (this.extraType == null) this.extraType = ExtraType.NONE;
        if (this.extraRuns == null) this.extraRuns = 0;
        if (this.totalRuns == null) this.totalRuns = this.batterRuns + this.extraRuns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatchInnings getInnings() {
        return innings;
    }

    public void setInnings(MatchInnings innings) {
        this.innings = innings;
    }

    public Integer getBallSequence() {
        return ballSequence;
    }

    public void setBallSequence(Integer ballSequence) {
        this.ballSequence = ballSequence;
    }

    public Integer getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(Integer overNumber) {
        this.overNumber = overNumber;
    }

    public Integer getBallInOver() {
        return ballInOver;
    }

    public void setBallInOver(Integer ballInOver) {
        this.ballInOver = ballInOver;
    }

    public Player getBowler() {
        return bowler;
    }

    public void setBowler(Player bowler) {
        this.bowler = bowler;
    }

    public Player getStriker() {
        return striker;
    }

    public void setStriker(Player striker) {
        this.striker = striker;
    }

    public Player getNonStriker() {
        return nonStriker;
    }

    public void setNonStriker(Player nonStriker) {
        this.nonStriker = nonStriker;
    }

    public Integer getBatterRuns() {
        return batterRuns;
    }

    public void setBatterRuns(Integer batterRuns) {
        this.batterRuns = batterRuns;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public Integer getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(Integer extraRuns) {
        this.extraRuns = extraRuns;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public boolean isLegal() {
        return isLegal;
    }

    public void setLegal(boolean legal) {
        isLegal = legal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public DismissalType getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(DismissalType dismissalType) {
        this.dismissalType = dismissalType;
    }

    public Player getDismissedPlayer() {
        return dismissedPlayer;
    }

    public void setDismissedPlayer(Player dismissedPlayer) {
        this.dismissedPlayer = dismissedPlayer;
    }

    public Player getFielder() {
        return fielder;
    }

    public void setFielder(Player fielder) {
        this.fielder = fielder;
    }

    public Player getNextBatter() {
        return nextBatter;
    }

    public void setNextBatter(Player nextBatter) {
        this.nextBatter = nextBatter;
    }
}
