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
