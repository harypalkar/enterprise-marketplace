package com.enterprise.marketplace.categoryservice.application.service;

import com.enterprise.marketplace.categoryservice.application.dto.CategoryPageResponse;
import com.enterprise.marketplace.categoryservice.application.dto.CategoryResponse;
import com.enterprise.marketplace.categoryservice.application.dto.CreateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.dto.UpdateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.mapper.CategoryMapper;
import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryPage;
import com.enterprise.marketplace.categoryservice.domain.model.CategorySearchCriteria;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import com.enterprise.marketplace.categoryservice.domain.port.CategoryRepository;
import com.enterprise.marketplace.categoryservice.domain.service.CategoryDomainService;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Category slug already exists: " + request.getSlug());
        }

        Category category = categoryMapper.toDomain(request);
        try {
            CategoryDomainService.validateForCreate(category);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository
                .findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", slug));
        return categoryMapper.toResponse(category);
    }

    public CategoryPageResponse searchCategories(
            CategoryStatus status, UUID parentId, String keyword, int page, int size, String sort) {
        CategorySearchCriteria criteria = CategorySearchCriteria.builder()
                .status(status)
                .parentId(parentId)
                .keyword(StringUtils.hasText(keyword) ? keyword.trim() : null)
                .build();

        CategoryPage<Category> result = categoryRepository.search(criteria, page, size, sort);
        List<CategoryResponse> content =
                result.content().stream().map(categoryMapper::toResponse).toList();

        return CategoryPageResponse.builder()
                .content(content)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .page(result.pageNumber())
                .size(result.pageSize())
                .build();
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category existing = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Category slug already exists: " + request.getSlug());
        }

        Category updated = existing.toBuilder()
                .slug(request.getSlug())
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .displayOrder(request.getDisplayOrder())
                .status(request.getStatus())
                .build();

        try {
            CategoryDomainService.validateForUpdate(updated);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }

        return categoryMapper.toResponse(categoryRepository.save(updated));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }
}
