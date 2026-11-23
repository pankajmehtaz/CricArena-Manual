package com.cricarena.backend.dto.request;

import com.cricarena.backend.entity.enums.BattingStyle;
import com.cricarena.backend.entity.enums.BowlingStyle;
import com.cricarena.backend.entity.enums.PlayingRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public class PlayerRequest {

    @NotBlank(message = "Player full name is required")
    @Size(max = 100, message = "Player full name must not exceed 100 characters")
    private String fullName;

    @Min(value = 0, message = "Jersey number cannot be negative")
    @Max(value = 999, message = "Jersey number cannot exceed 999")
    private Integer jerseyNumber;

    @NotNull(message = "Playing role is required")
    private PlayingRole playingRole;

    @NotNull(message = "Batting style is required")
    private BattingStyle battingStyle;

    private BowlingStyle bowlingStyle;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    @Pattern(
        regexp = "^[0-9+ -]*$",
        message = "Contact phone may only contain digits, '+', spaces, and hyphens"
    )
    private String contactPhone;

    @Size(max = 255, message = "Profile image URL must not exceed 255 characters")
    @URL(regexp = "^(https?://).*$", message = "Profile image URL must be a valid HTTP or HTTPS URL")
    private String profileImageUrl;

    @NotNull(message = "Team ID is required")
    @Positive(message = "Team ID must be positive")
    private Long teamId;

    public PlayerRequest() {
    }

    public PlayerRequest(String fullName, Integer jerseyNumber, PlayingRole playingRole,
                         BattingStyle battingStyle, BowlingStyle bowlingStyle, LocalDate dateOfBirth,
                         String contactEmail, String contactPhone, String profileImageUrl, Long teamId) {
        this.fullName = fullName;
        this.jerseyNumber = jerseyNumber;
        this.playingRole = playingRole;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
        this.dateOfBirth = dateOfBirth;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.profileImageUrl = profileImageUrl;
        this.teamId = teamId;
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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}
