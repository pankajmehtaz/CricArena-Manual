package com.cricarena.backend.dto.response;

import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.BowlingStyle;
import com.cricarena.backend.entity.enums.PlayingRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PlayerResponse {

    private Long id;
    private String fullName;
    private Integer jerseyNumber;
    private PlayingRole playingRole;
    private BattingStyle battingStyle;
    private BowlingStyle bowlingStyle;
    private LocalDate dateOfBirth;
    private String contactEmail;
    private String contactPhone;
    private String profileImageUrl;
    private boolean active;

    private Long teamId;
    private String teamName;
    private String teamShortName;

    private Long collegeId;
    private String collegeName;
    private String collegeCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PlayerResponse() {
    }

    public PlayerResponse(Long id, String fullName, Integer jerseyNumber, PlayingRole playingRole,
                          BattingStyle battingStyle, BowlingStyle bowlingStyle, LocalDate dateOfBirth,
                          String contactEmail, String contactPhone, String profileImageUrl,
                          boolean active, Long teamId, String teamName, String teamShortName,
                          Long collegeId, String collegeName, String collegeCode,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.jerseyNumber = jerseyNumber;
        this.playingRole = playingRole;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
        this.dateOfBirth = dateOfBirth;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.profileImageUrl = profileImageUrl;
        this.active = active;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamShortName = teamShortName;
        this.collegeId = collegeId;
        this.collegeName = collegeName;
        this.collegeCode = collegeCode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public PlayingRole getPlayingRole() {
        return playingRole;
    }

    public void setPlayingRole(PlayingRole playingRole) {
        this.playingRole = playingRole;
    }

    public BattingStyle getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(BattingStyle battingStyle) {
        this.battingStyle = battingStyle;
    }

    public BowlingStyle getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(BowlingStyle bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamShortName() {
        return teamShortName;
    }

    public void setTeamShortName(String teamShortName) {
        this.teamShortName = teamShortName;
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

    public String getCollegeCode() {
        return collegeCode;
    }

    public void setCollegeCode(String collegeCode) {
        this.collegeCode = collegeCode;
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
}
