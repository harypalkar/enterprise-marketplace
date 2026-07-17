package com.enterprise.marketplace.identityservice.application.dto;

import com.enterprise.marketplace.identityservice.domain.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetUserTypeRequest {
    @NotBlank
    private String verificationToken;

    @NotNull
    private UserType userType;
}
