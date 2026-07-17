package com.enterprise.marketplace.identityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendOtpRequest {
    @NotBlank
    private String countryCode = "+91";

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "mobileNumber must be a valid 10-digit Indian mobile")
    private String mobileNumber;
}
