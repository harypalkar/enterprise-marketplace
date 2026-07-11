package com.enterprise.marketplace.searchservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchResult {

    private String productId;
    private String sku;
    private String name;
    private String description;
    private String sellerId;
    private String categoryId;
    private String status;
    private BigDecimal unitPrice;
    private String currency;
    private String unitOfMeasure;
    private Instant indexedAt;
    private Double score;
}
