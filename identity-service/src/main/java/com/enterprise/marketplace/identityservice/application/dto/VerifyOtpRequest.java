package com.enterprise.marketplace.identityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank
    private String sessionId;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "otp must be 6 digits")
    private String otp;
}
