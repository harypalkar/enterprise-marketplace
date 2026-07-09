package com.enterprise.marketplace.categoryservice.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CategorySearchCriteria {

    CategoryStatus status;
    UUID parentId;
    String keyword;
}
