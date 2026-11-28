package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.InvitationStatus;
import com.cricarena.backend.entity.enums.UserRole;

import java.time.LocalDateTime;

public class UserInvitationResponse {

    private Long id;
    private String email;
    private UserRole role;
    private Long collegeId;
    private Long teamId;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime createdAt;
    private String collegeName;
    private String invitationUrl;

    public UserInvitationResponse() {
    }

    public UserInvitationResponse(Long id, String email, UserRole role, Long collegeId, Long teamId,
                                  InvitationStatus status, LocalDateTime expiresAt,
                                  LocalDateTime acceptedAt, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.collegeId = collegeId;
        this.teamId = teamId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.acceptedAt = acceptedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getInvitationUrl() {
        return invitationUrl;
    }

    public void setInvitationUrl(String invitationUrl) {
        this.invitationUrl = invitationUrl;
    }
}
