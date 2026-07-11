package com.enterprise.marketplace.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchInterpretRequest {

    @NotBlank
    String query;
}
