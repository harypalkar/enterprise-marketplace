package com.enterprise.marketplace.aiservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GenerateDescriptionRequest {

    @NotNull
    UUID productId;

    @NotBlank
    String name;

    String sku;

    UUID categoryId;

    Map<String, Object> attributes;
}
