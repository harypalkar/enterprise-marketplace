package com.enterprise.marketplace.categoryservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.With;

/**
 * Category aggregate root (domain model - no framework dependencies).
 */
@Value
@Builder(toBuilder = true)
@With
public class Category {

    UUID id;
    String slug;
    String name;
    String description;
    UUID parentId;
    Integer displayOrder;
    CategoryStatus status;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    Long version;
}
