package com.enterprise.marketplace.identityservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QrCreateResponse {
    String qrSessionId;
    String qrPayload;
    long expiresInSeconds;
    String status;
}
