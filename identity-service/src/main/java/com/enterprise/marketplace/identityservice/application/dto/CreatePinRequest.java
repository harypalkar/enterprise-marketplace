package com.enterprise.marketplace.identityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePinRequest {
    @NotBlank
    private String verificationToken;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "pin must be 6 digits")
    private String pin;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "confirmPin must be 6 digits")
    private String confirmPin;
}
