package com.cricarena.backend.dto.request;

import com.cricarena.backend.entity.enums.ExtraType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RecordDeliveryRequest {

    @NotNull(message = "Batter runs is required")
    @Min(value = 0, message = "Batter runs cannot be negative")
    @Max(value = 6, message = "Batter runs cannot exceed 6")
    private Integer batterRuns = 0;

    @NotNull(message = "Extra type is required")
    private ExtraType extraType = ExtraType.NONE;

    @NotNull(message = "Extra runs is required")
    @Min(value = 0, message = "Extra runs cannot be negative")
    private Integer extraRuns = 0;

    private boolean isWicket = false;

    private com.cricarena.backend.entity.enums.DismissalType dismissalType;

    private Long dismissedPlayerId;

    private Long fielderId;

    private Long nextBatterPlayerId;

    public RecordDeliveryRequest() {
    }

    public RecordDeliveryRequest(Integer batterRuns, ExtraType extraType, Integer extraRuns) {
        this(batterRuns, extraType, extraRuns, false, null, null, null, null);
    }

    public RecordDeliveryRequest(Integer batterRuns, ExtraType extraType, Integer extraRuns,
                                 boolean isWicket, com.cricarena.backend.entity.enums.DismissalType dismissalType,
                                 Long dismissedPlayerId, Long fielderId, Long nextBatterPlayerId) {
        this.batterRuns = batterRuns;
        this.extraType = extraType != null ? extraType : ExtraType.NONE;
        this.extraRuns = extraRuns != null ? extraRuns : 0;
        this.isWicket = isWicket;
        this.dismissalType = dismissalType;
        this.dismissedPlayerId = dismissedPlayerId;
        this.fielderId = fielderId;
        this.nextBatterPlayerId = nextBatterPlayerId;
    }

    public boolean isWicket() {
        return isWicket;
    }

    public boolean getIsWicket() {
        return isWicket;
    }

    public void setWicket(boolean wicket) {
        isWicket = wicket;
    }

    public void setIsWicket(boolean isWicket) {
        this.isWicket = isWicket;
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

    public Long getFielderId() {
        return fielderId;
    }

    public void setFielderId(Long fielderId) {
        this.fielderId = fielderId;
    }

    public Long getNextBatterPlayerId() {
        return nextBatterPlayerId;
    }

    public void setNextBatterPlayerId(Long nextBatterPlayerId) {
        this.nextBatterPlayerId = nextBatterPlayerId;
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
}
