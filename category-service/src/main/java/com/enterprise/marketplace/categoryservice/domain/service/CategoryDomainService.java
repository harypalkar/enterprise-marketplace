package com.enterprise.marketplace.categoryservice.domain.service;

import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import org.springframework.util.StringUtils;

/**
 * Pure domain rules for category validation.
 */
public final class CategoryDomainService {

    private CategoryDomainService() {
    }

    public static void validateForCreate(Category category) {
        validateSlug(category.getSlug());
        validateName(category.getName());
        validateDisplayOrder(category.getDisplayOrder());
        validateStatus(category.getStatus());
    }

    public static void validateForUpdate(Category category) {
        validateSlug(category.getSlug());
        validateName(category.getName());
        validateDisplayOrder(category.getDisplayOrder());
        validateStatus(category.getStatus());
    }

    private static void validateSlug(String slug) {
        if (!StringUtils.hasText(slug) || slug.length() > 120) {
            throw new IllegalArgumentException("Slug is required and must be at most 120 characters");
        }
    }

    private static void validateName(String name) {
        if (!StringUtils.hasText(name) || name.length() > 255) {
            throw new IllegalArgumentException("Name is required and must be at most 255 characters");
        }
    }

    private static void validateDisplayOrder(Integer displayOrder) {
        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("Display order must be zero or positive");
        }
    }

    private static void validateStatus(CategoryStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}
