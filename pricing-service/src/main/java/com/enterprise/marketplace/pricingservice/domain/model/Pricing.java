package com.enterprise.marketplace.pricingservice.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Pricing aggregate root (domain model — no framework dependencies).
 */
@Value
@Builder(toBuilder = true)
@With
public class Pricing {

    UUID id;
    UUID productId;
    UUID sellerId;
    BigDecimal unitPrice;
    String currency;
    Integer minQuantity;
    BigDecimal discountPercent;
    Instant validFrom;
    Instant validTo;
    PricingStatus status;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    Long version;
}
