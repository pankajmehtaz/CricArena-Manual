package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CaptainApplicationReviewRequest {

    @NotNull(message = "Review decision (approved true/false) is required")
    private Boolean approved;

    @Size(max = 255, message = "Rejection reason must not exceed 255 characters")
    private String rejectionReason;

    public CaptainApplicationReviewRequest() {
    }

    public CaptainApplicationReviewRequest(Boolean approved, String rejectionReason) {
        this.approved = approved;
        this.rejectionReason = rejectionReason;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
