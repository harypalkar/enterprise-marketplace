package com.enterprise.marketplace.pricingservice.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdatePricingRequest {

    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal unitPrice;

    @Size(min = 3, max = 3)
    String currency;

    @Positive
    Integer minQuantity;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    BigDecimal discountPercent;

    Instant validFrom;

    Instant validTo;
}
