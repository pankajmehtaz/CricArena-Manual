package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.ExtraType;

import java.time.LocalDateTime;

public class DeliveryResponse {

    private Long id;
    private Long inningsId;
    private Integer ballSequence;
    private Integer overNumber;
    private Integer ballInOver;
    private String overText; // e.g. "0.1", "1.6"
    private Long bowlerId;
    private String bowlerName;
    private Long strikerId;
    private String strikerName;
    private Long nonStrikerId;
    private String nonStrikerName;
    private Integer batterRuns;
    private ExtraType extraType;
    private Integer extraRuns;
    private Integer totalRuns;
    private boolean legal;
    private boolean wicket;
    private com.cricarena.backend.entity.enums.DismissalType dismissalType;
    private Long dismissedPlayerId;
    private String dismissedPlayerName;
    private Long fielderId;
    private String fielderName;
    private Long nextBatterId;
    private String nextBatterName;
    private LocalDateTime createdAt;

    public DeliveryResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInningsId() {
        return inningsId;
    }

    public void setInningsId(Long inningsId) {
        this.inningsId = inningsId;
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

    public String getOverText() {
        return overText;
    }

    public void setOverText(String overText) {
        this.overText = overText;
    }

    public Long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(Long bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName;
    }

    public Long getStrikerId() {
        return strikerId;
    }

    public void setStrikerId(Long strikerId) {
        this.strikerId = strikerId;
    }

    public String getStrikerName() {
        return strikerName;
    }

    public void setStrikerName(String strikerName) {
        this.strikerName = strikerName;
    }

    public Long getNonStrikerId() {
        return nonStrikerId;
    }

    public void setNonStrikerId(Long nonStrikerId) {
        this.nonStrikerId = nonStrikerId;
    }

    public String getNonStrikerName() {
        return nonStrikerName;
    }

    public void setNonStrikerName(String nonStrikerName) {
        this.nonStrikerName = nonStrikerName;
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
        return legal;
    }

    public void setLegal(boolean legal) {
        this.legal = legal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isWicket() {
        return wicket;
    }

    public void setWicket(boolean wicket) {
        this.wicket = wicket;
    }

    public com.cricarena.backend.entity.enums.DismissalType getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(com.cricarena.backend.entity.enums.DismissalType dismissalType) {
        this.dismissalType = dismissalType;
    }

    public Long getDismissedPlayerId() {
        return dismissedPlayerId;
    }

    public void setDismissedPlayerId(Long dismissedPlayerId) {
        this.dismissedPlayerId = dismissedPlayerId;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(String dismissedPlayerName) {
        this.dismissedPlayerName = dismissedPlayerName;
    }

    public Long getFielderId() {
        return fielderId;
    }

    public void setFielderId(Long fielderId) {
        this.fielderId = fielderId;
    }

    public String getFielderName() {
        return fielderName;
    }

    public void setFielderName(String fielderName) {
        this.fielderName = fielderName;
    }

    public Long getNextBatterId() {
        return nextBatterId;
    }

    public void setNextBatterId(Long nextBatterId) {
        this.nextBatterId = nextBatterId;
    }

    public String getNextBatterName() {
        return nextBatterName;
    }

    public void setNextBatterName(String nextBatterName) {
        this.nextBatterName = nextBatterName;
    }
}
