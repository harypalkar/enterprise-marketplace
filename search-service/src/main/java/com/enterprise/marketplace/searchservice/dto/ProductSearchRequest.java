package com.enterprise.marketplace.searchservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {

    private String q;
    private UUID sellerId;
    private UUID categoryId;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    @Min(0)
    @Builder.Default
    private int page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sort = "indexedAt,desc";
}
