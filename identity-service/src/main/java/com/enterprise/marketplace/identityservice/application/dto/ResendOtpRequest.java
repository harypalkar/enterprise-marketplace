package com.enterprise.marketplace.identityservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @NotBlank
    private String sessionId;
}
