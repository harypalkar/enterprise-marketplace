package com.enterprise.marketplace.sellerservice.application.dto;

import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateSellerStatusRequest {

    @NotNull
    SellerStatus status;
}
