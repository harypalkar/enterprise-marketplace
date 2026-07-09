package com.enterprise.marketplace.inventoryservice.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AdjustInventoryQuantityRequest {

    @NotNull
    @Min(1)
    Integer quantity;
}
