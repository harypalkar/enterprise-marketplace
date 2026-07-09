package com.enterprise.marketplace.sellerservice.domain.model;

import java.util.List;

/**
 * Domain-neutral paginated result.
 */
public record SellerPage<T>(List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {}
