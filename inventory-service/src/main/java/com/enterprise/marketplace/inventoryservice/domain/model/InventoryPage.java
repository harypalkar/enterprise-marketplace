package com.enterprise.marketplace.inventoryservice.domain.model;

import java.util.List;

/**
 * Domain-neutral paginated result.
 */
public record InventoryPage<T>(
        List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {}
