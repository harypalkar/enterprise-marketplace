package com.enterprise.marketplace.productservice.dto.canonical;

import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.enterprise.marketplace.productservice.enums.ProductWorkflowStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailResponse {

    UUID id;
    String sku;
    String name;
    String description;
    UUID sellerId;
    UUID categoryId;
    String unitOfMeasure;
    String hsnCode;
    ProductStatus status;
    BigDecimal unitPrice;
    String currency;
    Integer minOrderQuantity;
    Instant createdAt;
    Instant updatedAt;
    PriceDetail price;
    InventoryDetail inventory;
    List<AttributeDetail> attributes;
    List<ImageDetail> images;
    List<DocumentDetail> documents;
    WorkflowDetail workflow;
    Map<String, String> tags;

    @Value
    @Builder
    @Jacksonized
    public static class PriceDetail {
        UUID id;
        BigDecimal unitPrice;
        String currency;
        Integer minQuantity;
        BigDecimal discountPercent;
        Instant validFrom;
        Instant validTo;
    }

    @Value
    @Builder
    @Jacksonized
    public static class InventoryDetail {
        UUID id;
        Integer quantityAvailable;
        Integer quantityReserved;
        Integer reorderLevel;
        String warehouseCode;
    }

    @Value
    @Builder
    @Jacksonized
    public static class AttributeDetail {
        UUID id;
        String key;
        String value;
    }

    @Value
    @Builder
    @Jacksonized
    public static class ImageDetail {
        UUID id;
        String url;
        String altText;
        Integer displayOrder;
        boolean primaryImage;
    }

    @Value
    @Builder
    @Jacksonized
    public static class DocumentDetail {
        UUID id;
        String documentType;
        String url;
        String fileName;
    }

    @Value
    @Builder
    @Jacksonized
    public static class WorkflowDetail {
        UUID id;
        ProductWorkflowStatus status;
        ProductWorkflowStatus previousStatus;
        String message;
        Instant updatedAt;
    }
}
