package com.enterprise.marketplace.inventoryservice.application.dto;

import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreateInventoryRequest {

    @NotNull
    UUID productId;

    @NotNull
    UUID sellerId;

    @NotNull
    @Min(0)
    Integer quantityAvailable;

    @Min(0)
    Integer quantityReserved;

    @Min(0)
    Integer reorderLevel;

    @Size(max = 32)
    String warehouseCode;

    InventoryStatus status;
}
