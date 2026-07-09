package com.enterprise.marketplace.buyerservice.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateBuyerRequest {

    @NotBlank
    @Size(max = 255)
    String companyName;

    @NotBlank
    @Size(max = 128)
    String contactPerson;

    @NotBlank
    @Email
    @Size(max = 255)
    String email;

    @NotBlank
    @Size(max = 32)
    @Pattern(regexp = "^[+0-9()\\-\\s]{7,32}$")
    String phone;

    @NotBlank
    @Size(max = 100)
    String city;

    @NotBlank
    @Size(max = 100)
    String state;

    @Size(max = 100)
    String country;

    @NotBlank
    @Size(max = 12)
    @Pattern(regexp = "^[A-Za-z0-9\\-\\s]{3,12}$")
    String pinCode;
}
