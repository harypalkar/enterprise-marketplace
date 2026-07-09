package com.enterprise.marketplace.inventoryservice.application.dto;

import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateInventoryRequest {

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
