package com.enterprise.marketplace.pricingservice.application.dto;

import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdatePricingStatusRequest {

    @NotNull
    PricingStatus status;
}
