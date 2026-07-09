package com.enterprise.marketplace.productservice.dto.canonical;

import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSearchRequest {

    ProductStatus status;

    UUID sellerId;

    UUID categoryId;

    String keyword;

    @Builder.Default
    int page = 0;

    @Builder.Default
    int size = 20;

    @Builder.Default
    String sort = "createdAt,desc";
}
