package com.enterprise.marketplace.subscriptionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscribeRequest {

    @NotBlank
    @Size(max = 64)
    String requestId;

    @Size(max = 64)
    String correlationId;

    @NotNull
    UUID sellerId;

    @NotNull
    UUID buyerId;

    @NotBlank
    @Size(max = 32)
    String planCode;

    Boolean autoRenew;
}
