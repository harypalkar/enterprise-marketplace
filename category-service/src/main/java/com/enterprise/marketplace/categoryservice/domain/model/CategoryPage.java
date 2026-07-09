package com.enterprise.marketplace.categoryservice.domain.model;

import java.util.List;

/**
 * Domain-neutral paginated result.
 */
public record CategoryPage<T>(List<T> content, long totalElements, int totalPages, int pageNumber, int pageSize) {}
