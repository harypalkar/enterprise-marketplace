package com.enterprise.marketplace.categoryservice.application.mapper;

import com.enterprise.marketplace.categoryservice.application.dto.CategoryResponse;
import com.enterprise.marketplace.categoryservice.application.dto.CreateCategoryRequest;
import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = CategoryStatus.class)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : CategoryStatus.ACTIVE)")
    @Mapping(target = "displayOrder", expression = "java(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)")
    Category toDomain(CreateCategoryRequest request);
}
