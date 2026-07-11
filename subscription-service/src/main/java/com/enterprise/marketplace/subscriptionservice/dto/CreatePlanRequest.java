package com.enterprise.marketplace.subscriptionservice.dto;

import com.enterprise.marketplace.subscriptionservice.enums.BillingCycle;
import com.enterprise.marketplace.subscriptionservice.enums.PlanTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreatePlanRequest {

    @NotBlank
    @Size(max = 32)
    String planCode;

    @NotBlank
    @Size(max = 128)
    String name;

    @NotNull
    PlanTier tier;

    @NotNull
    @PositiveOrZero
    BigDecimal price;

    @NotBlank
    @Size(min = 3, max = 3)
    String currency;

    @NotNull
    BillingCycle billingCycle;

    Map<String, Object> features;
}
