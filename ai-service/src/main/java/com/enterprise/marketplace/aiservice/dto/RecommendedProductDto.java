package com.enterprise.marketplace.aiservice.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecommendedProductDto {

    UUID productId;
    String name;
    String reason;
}
