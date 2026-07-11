package com.enterprise.marketplace.subscriptionservice.dto;

import com.enterprise.marketplace.subscriptionservice.enums.BillingCycle;
import com.enterprise.marketplace.subscriptionservice.enums.PlanTier;
import com.enterprise.marketplace.subscriptionservice.enums.SubscriptionStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubscriptionResponse {

    UUID id;
    String requestId;
    UUID sellerId;
    UUID buyerId;
    UUID planId;
    String planCode;
    String planName;
    PlanTier planTier;
    BillingCycle billingCycle;
    SubscriptionStatus status;
    LocalDate startDate;
    LocalDate endDate;
    Boolean autoRenew;
    Boolean active;
    Instant createdAt;
    Instant updatedAt;
}
