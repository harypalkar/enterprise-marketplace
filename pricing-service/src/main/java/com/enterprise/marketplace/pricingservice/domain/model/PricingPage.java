package com.enterprise.marketplace.pricingservice.domain.model;

import java.util.List;

/**
 * Domain-neutral paginated result.
 */
public record PricingPage<T>(List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {}
