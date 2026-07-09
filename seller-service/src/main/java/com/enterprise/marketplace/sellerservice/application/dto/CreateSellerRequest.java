package com.enterprise.marketplace.sellerservice.application.dto;

import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
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
public class CreateSellerRequest {

    @NotBlank
    @Size(max = 255)
    String companyName;

    @NotBlank
    @Size(max = 255)
    String tradeName;

    @NotBlank
    @Size(max = 15)
    String gstin;

    @NotBlank
    @Size(max = 10)
    String pan;

    @NotBlank
    @Email
    @Size(max = 255)
    String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    String phone;

    @NotBlank
    @Size(max = 128)
    String city;

    @NotBlank
    @Size(max = 128)
    String state;

    @Size(max = 128)
    String country;

    @NotBlank
    @Size(max = 10)
    String pinCode;

    SellerStatus status;
}
