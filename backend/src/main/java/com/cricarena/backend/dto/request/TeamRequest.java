package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 150, message = "Team name must not exceed 150 characters")
    private String name;

    @Size(max = 20, message = "Short name must not exceed 20 characters")
    private String shortName;

    @Size(max = 150, message = "Home ground must not exceed 150 characters")
    private String homeGround;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @NotNull(message = "College ID is required")
    private Long collegeId;

    public TeamRequest() {
    }

    public TeamRequest(String name, String shortName, String homeGround, String city,
                       String contactEmail, Long collegeId) {
        this.name = name;
        this.shortName = shortName;
        this.homeGround = homeGround;
        this.city = city;
        this.contactEmail = contactEmail;
        this.collegeId = collegeId;
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

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }
}
