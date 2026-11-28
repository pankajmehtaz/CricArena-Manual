package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CaptainApplicationSubmitRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "College ID is required")
    private Long collegeId;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+ -]{7,20}$", message = "Invalid contact number format")
    private String contactNumber;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 2000, message = "Reason must be between 10 and 2000 characters")
    private String reason;

    @Size(max = 2000, message = "Experience description must not exceed 2000 characters")
    private String experience;

    public CaptainApplicationSubmitRequest() {
    }

    public CaptainApplicationSubmitRequest(Long tournamentId, Long collegeId, String contactNumber, String reason, String experience) {
        this.tournamentId = tournamentId;
        this.collegeId = collegeId;
        this.contactNumber = contactNumber;
        this.reason = reason;
        this.experience = experience;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }
}
