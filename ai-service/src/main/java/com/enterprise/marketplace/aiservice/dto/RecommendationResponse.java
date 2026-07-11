package com.enterprise.marketplace.aiservice.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RecommendationResponse {

    String buyerId;
    List<RecommendedProductDto> recommendations;
    String model;
}
