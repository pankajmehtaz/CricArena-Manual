package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.CaptainApplicationStatus;

import java.time.LocalDateTime;

public class CaptainApplicationResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long tournamentId;
    private String tournamentName;
    private Long collegeId;
    private String collegeName;
    private CaptainApplicationStatus status;
    private String contactNumber;
    private String reason;
    private String experience;
    private Long reviewedByUserId;
    private String reviewedByUserName;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canCreateTeam;
    private Long ownedTeamId;

    public CaptainApplicationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
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

    public CaptainApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(CaptainApplicationStatus status) {
        this.status = status;
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

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(Long reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public String getReviewedByUserName() {
        return reviewedByUserName;
    }

    public void setReviewedByUserName(String reviewedByUserName) {
        this.reviewedByUserName = reviewedByUserName;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public boolean isCanCreateTeam() {
        return canCreateTeam;
    }

    public void setCanCreateTeam(boolean canCreateTeam) {
        this.canCreateTeam = canCreateTeam;
    }

    public Long getOwnedTeamId() {
        return ownedTeamId;
    }

    public void setOwnedTeamId(Long ownedTeamId) {
        this.ownedTeamId = ownedTeamId;
    }
}
