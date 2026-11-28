package com.cricarena.backend.dto.request;

import com.cricarena.backend.entity.enums.TournamentFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TournamentRequest {

    @NotBlank(message = "Tournament name is required")
    @Size(max = 150, message = "Tournament name must not exceed 150 characters")
    private String name;

    private String description;

    @NotNull(message = "Tournament format is required")
    private TournamentFormat format;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Long collegeId;

    private java.time.LocalDateTime registrationOpensAt;

    private java.time.LocalDateTime registrationDeadline;

    public TournamentRequest() {
    }

    public TournamentRequest(String name, String description, TournamentFormat format, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.description = description;
        this.format = format;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @AssertTrue(message = "End date must not be before start date")
    public boolean isEndDateValid() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Registration deadline must not be before registration opening date")
    public boolean isRegistrationDeadlineValid() {
        if (registrationOpensAt == null || registrationDeadline == null) {
            return true;
        }
        return !registrationDeadline.isBefore(registrationOpensAt);
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public java.time.LocalDateTime getRegistrationOpensAt() {
        return registrationOpensAt;
    }

    public void setRegistrationOpensAt(java.time.LocalDateTime registrationOpensAt) {
        this.registrationOpensAt = registrationOpensAt;
    }

    public java.time.LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(java.time.LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TournamentFormat getFormat() {
        return format;
    }

    public void setFormat(TournamentFormat format) {
        this.format = format;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
