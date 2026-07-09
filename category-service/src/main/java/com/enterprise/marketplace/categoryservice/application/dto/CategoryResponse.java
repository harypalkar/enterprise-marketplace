package com.enterprise.marketplace.categoryservice.application.dto;

import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CategoryResponse {

    UUID id;
    String slug;
    String name;
    String description;
    UUID parentId;
    Integer displayOrder;
    CategoryStatus status;
    Instant createdAt;
    Instant updatedAt;
    Long version;
}
