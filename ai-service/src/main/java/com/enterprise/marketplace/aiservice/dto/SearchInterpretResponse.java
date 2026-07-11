package com.enterprise.marketplace.aiservice.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchInterpretResponse {

    String q;
    UUID categoryId;
    BigDecimal minPrice;
    BigDecimal maxPrice;
    String status;
    String rawInterpretation;
}
