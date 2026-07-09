package com.enterprise.marketplace.productservice.dto.canonical;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestInfoDto {

    @NotBlank
    String clientRequestId;

    String idempotencyKey;

    String correlationId;

    String requestId;

    String requestedBy;
}
