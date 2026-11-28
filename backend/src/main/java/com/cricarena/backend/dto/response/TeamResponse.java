package com.cricarena.backend.dto.response;

import java.time.LocalDateTime;

public class TeamResponse {

    private Long id;
    private String name;
    private String shortName;
    private String homeGround;
    private String city;
    private String contactEmail;
    private boolean active;
    private Long collegeId;
    private String collegeName;
    private String collegeCode;
    private Long captainUserId;
    private String captainName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeamResponse() {
    }

    public TeamResponse(Long id, String name, String shortName, String homeGround,
                        String city, String contactEmail, boolean active, Long collegeId,
                        String collegeName, String collegeCode, LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.homeGround = homeGround;
        this.city = city;
        this.contactEmail = contactEmail;
        this.active = active;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getHomeGround() {
        return homeGround;
    }

    public void setHomeGround(String homeGround) {
        this.homeGround = homeGround;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Long getCaptainUserId() {
        return captainUserId;
    }

    public void setCaptainUserId(Long captainUserId) {
        this.captainUserId = captainUserId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
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
