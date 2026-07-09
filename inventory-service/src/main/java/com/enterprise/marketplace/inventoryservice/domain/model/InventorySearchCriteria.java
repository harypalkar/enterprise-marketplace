package com.enterprise.marketplace.inventoryservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventorySearchCriteria {

    UUID productId;
    UUID sellerId;
    InventoryStatus status;
}
