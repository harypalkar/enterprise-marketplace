package com.enterprise.marketplace.inventoryservice.application.dto;

import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventoryResponse {

    UUID id;
    UUID productId;
    UUID sellerId;
    Integer quantityAvailable;
    Integer quantityReserved;
    Integer reorderLevel;
    String warehouseCode;
    InventoryStatus status;
    Instant createdAt;
    Instant updatedAt;
    Long version;
}
