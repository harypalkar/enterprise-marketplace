package com.enterprise.marketplace.identityservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetUserDetailsRequest {
    @NotBlank
    private String verificationToken;

    private String fullName;
    private String legalName;

    @Email
    private String email;

    private String mobileNumber;
    private String companyName;
    private String website;
    private String gstNumber;
    private String city;
    private String country;
}
