package com.enterprise.marketplace.pricingservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PricingSearchCriteria {

    UUID productId;
    UUID sellerId;
    PricingStatus status;
}
