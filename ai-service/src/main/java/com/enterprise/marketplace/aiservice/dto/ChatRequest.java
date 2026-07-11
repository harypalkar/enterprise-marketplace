package com.enterprise.marketplace.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatRequest {

    String sessionKey;

    @NotBlank
    @Size(max = 4000)
    String message;

    String userId;

    String userRole;
}
