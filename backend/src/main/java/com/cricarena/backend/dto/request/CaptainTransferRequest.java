package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class CaptainTransferRequest {

    @NotNull(message = "New captain user ID is required")
    private Long newCaptainUserId;

    public CaptainTransferRequest() {
    }

    public CaptainTransferRequest(Long newCaptainUserId) {
        this.newCaptainUserId = newCaptainUserId;
    }

    public Long getNewCaptainUserId() {
        return newCaptainUserId;
    }

    public void setNewCaptainUserId(Long newCaptainUserId) {
        this.newCaptainUserId = newCaptainUserId;
    }
}
