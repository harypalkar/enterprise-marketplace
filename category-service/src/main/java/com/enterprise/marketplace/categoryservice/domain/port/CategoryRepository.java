package com.enterprise.marketplace.categoryservice.domain.port;

import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryPage;
import com.enterprise.marketplace.categoryservice.domain.model.CategorySearchCriteria;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for category persistence.
 */
public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    CategoryPage<Category> search(CategorySearchCriteria criteria, int page, int size, String sort);

    void deleteById(UUID id);
}
