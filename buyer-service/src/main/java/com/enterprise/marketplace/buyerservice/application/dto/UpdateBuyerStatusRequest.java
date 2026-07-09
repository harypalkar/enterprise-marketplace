package com.enterprise.marketplace.buyerservice.application.dto;

import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateBuyerStatusRequest {

    @NotNull
    BuyerStatus status;
}
