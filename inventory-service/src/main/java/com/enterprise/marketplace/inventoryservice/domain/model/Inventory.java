package com.enterprise.marketplace.inventoryservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Inventory aggregate root.
 */
@Value
@Builder(toBuilder = true)
@With
public class Inventory {

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
    String createdBy;
    String updatedBy;
    Long version;
}
