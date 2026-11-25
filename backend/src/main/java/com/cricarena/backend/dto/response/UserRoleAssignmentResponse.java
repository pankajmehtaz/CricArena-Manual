package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.UserRole;

import java.time.LocalDateTime;

public class UserRoleAssignmentResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private UserRole role;
    private Long collegeId;
    private Long teamId;
    private boolean active;
    private LocalDateTime assignedAt;
    private LocalDateTime revokedAt;

    public UserRoleAssignmentResponse() {
    }

    public UserRoleAssignmentResponse(Long id, Long userId, String userFullName, String userEmail,
                                      UserRole role, Long collegeId, Long teamId, boolean active,
                                      LocalDateTime assignedAt, LocalDateTime revokedAt) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.role = role;
        this.collegeId = collegeId;
        this.teamId = teamId;
        this.active = active;
        this.assignedAt = assignedAt;
        this.revokedAt = revokedAt;
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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
