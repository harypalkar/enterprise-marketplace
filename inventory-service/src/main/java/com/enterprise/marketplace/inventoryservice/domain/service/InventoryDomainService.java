package com.enterprise.marketplace.inventoryservice.domain.service;

import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import org.springframework.util.StringUtils;

/**
 * Pure domain rules for inventory validation and stock transitions.
 */
public final class InventoryDomainService {

    private InventoryDomainService() {}

    public static void validateForCreate(Inventory inventory) {
        validateIdentity(inventory);
        validateQuantities(inventory.getQuantityAvailable(), inventory.getQuantityReserved(), inventory.getReorderLevel());
    }

    public static void validateForUpdate(Inventory inventory) {
        validateQuantities(inventory.getQuantityAvailable(), inventory.getQuantityReserved(), inventory.getReorderLevel());
    }

    public static InventoryStatus deriveStatus(Integer quantityAvailable, Integer reorderLevel) {
        int available = quantityAvailable == null ? 0 : quantityAvailable;
        int reorder = reorderLevel == null ? 0 : reorderLevel;
        if (available <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }
        if (available <= reorder) {
            return InventoryStatus.LOW_STOCK;
        }
        return InventoryStatus.IN_STOCK;
    }

    private static void validateIdentity(Inventory inventory) {
        if (inventory.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (inventory.getSellerId() == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        if (StringUtils.hasText(inventory.getWarehouseCode()) && inventory.getWarehouseCode().length() > 32) {
            throw new IllegalArgumentException("Warehouse code must be at most 32 characters");
        }
    }

    private static void validateQuantities(Integer quantityAvailable, Integer quantityReserved, Integer reorderLevel) {
        int available = quantityAvailable == null ? 0 : quantityAvailable;
        int reserved = quantityReserved == null ? 0 : quantityReserved;
        int reorder = reorderLevel == null ? 0 : reorderLevel;

        if (available < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }
        if (reserved < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reorder < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }
    }
}
