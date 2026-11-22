package com.cricarena.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CollegeRequest {

    @NotBlank(message = "College name is required")
    @Size(max = 150, message = "College name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "College code is required")
    @Size(max = 20, message = "College code must not exceed 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "College code must be alphanumeric and may include hyphens")
    private String code;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    @Pattern(
        regexp = "^[0-9+ -]*$",
        message = "Contact phone may only contain digits, '+', spaces, and hyphens"
    )
    private String contactPhone;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    public CollegeRequest() {
    }

    public CollegeRequest(String name, String code, String address, String city, String state,
                          String country, String contactEmail, String contactPhone, String website) {
        this.name = name;
        this.code = code;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
