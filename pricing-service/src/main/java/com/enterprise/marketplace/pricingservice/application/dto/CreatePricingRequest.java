package com.enterprise.marketplace.pricingservice.application.dto;

import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreatePricingRequest {

    @NotNull
    UUID productId;

    @NotNull
    UUID sellerId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal unitPrice;

    @NotNull
    @Size(min = 3, max = 3)
    String currency;

    @Positive
    Integer minQuantity;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    BigDecimal discountPercent;

    @NotNull
    Instant validFrom;

    Instant validTo;

    PricingStatus status;
}
