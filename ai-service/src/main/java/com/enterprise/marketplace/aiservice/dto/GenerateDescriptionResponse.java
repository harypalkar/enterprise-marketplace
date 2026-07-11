package com.enterprise.marketplace.aiservice.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GenerateDescriptionResponse {

    UUID productId;
    String description;
    String model;
    Long latencyMs;
}
