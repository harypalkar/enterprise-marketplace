package com.enterprise.marketplace.categoryservice.bootstrap.controller;

import com.enterprise.marketplace.categoryservice.application.dto.CategoryPageResponse;
import com.enterprise.marketplace.categoryservice.application.dto.CategoryResponse;
import com.enterprise.marketplace.categoryservice.application.dto.CreateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.dto.UpdateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.service.CategoryApplicationService;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category catalog management APIs")
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    @PostMapping
    @Idempotent
    @Operation(summary = "Create a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryApplicationService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Category created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryApplicationService.getCategoryById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(categoryApplicationService.getCategoryBySlug(slug)));
    }

    @GetMapping
    @Operation(summary = "Search categories with pagination")
    public ResponseEntity<ApiResponse<CategoryPageResponse>> searchCategories(
            @RequestParam(required = false) CategoryStatus status,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder,asc") String sort) {
        CategoryPageResponse response =
                categoryApplicationService.searchCategories(status, parentId, keyword, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(categoryApplicationService.updateCategory(id, request), "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryApplicationService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }
}
