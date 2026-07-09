package com.enterprise.marketplace.categoryservice.application.dto;

import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class UpdateCategoryRequest {

    @NotBlank
    @Size(max = 120)
    String slug;

    @NotBlank
    @Size(max = 255)
    String name;

    @Size(max = 5000)
    String description;

    UUID parentId;

    @NotNull
    @Min(0)
    Integer displayOrder;

    @NotNull
    CategoryStatus status;
}
