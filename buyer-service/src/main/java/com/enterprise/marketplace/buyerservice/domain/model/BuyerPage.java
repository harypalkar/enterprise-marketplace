package com.enterprise.marketplace.buyerservice.domain.model;

import java.util.List;

/**
 * Domain-neutral paginated result.
 */
public record BuyerPage<T>(List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {}
