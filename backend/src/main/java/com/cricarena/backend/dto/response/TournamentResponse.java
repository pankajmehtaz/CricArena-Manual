package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.TournamentFormat;
import com.cricarena.backend.entity.enums.TournamentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TournamentResponse {

    private Long id;
    private String name;
    private String description;
    private TournamentFormat format;
    private LocalDate startDate;
    private LocalDate endDate;
    private TournamentStatus status;
    private Long collegeId;
    private String collegeName;
    private LocalDateTime registrationOpensAt;
    private LocalDateTime registrationDeadline;
    private boolean isRegistrationOpen;
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TournamentResponse() {
    }

    public TournamentResponse(Long id, String name, String description, TournamentFormat format,
                              LocalDate startDate, LocalDate endDate, TournamentStatus status,
                              LocalDateTime scheduledPublishAt, LocalDateTime publishedAt,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.format = format;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.scheduledPublishAt = scheduledPublishAt;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledPublishAt() {
        return scheduledPublishAt;
    }

    public void setScheduledPublishAt(LocalDateTime scheduledPublishAt) {
        this.scheduledPublishAt = scheduledPublishAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public LocalDateTime getRegistrationOpensAt() {
        return registrationOpensAt;
    }

    public void setRegistrationOpensAt(LocalDateTime registrationOpensAt) {
        this.registrationOpensAt = registrationOpensAt;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public boolean isRegistrationOpen() {
        return isRegistrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        isRegistrationOpen = registrationOpen;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
