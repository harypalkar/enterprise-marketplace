package com.enterprise.marketplace.subscriptionservice.dto;

import com.enterprise.marketplace.subscriptionservice.enums.BillingCycle;
import com.enterprise.marketplace.subscriptionservice.enums.PlanTier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlanResponse {

    UUID id;
    String planCode;
    String name;
    PlanTier tier;
    BigDecimal price;
    String currency;
    BillingCycle billingCycle;
    Map<String, Object> features;
    Boolean active;
    Instant createdAt;
    Instant updatedAt;
}
