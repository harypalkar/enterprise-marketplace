package com.enterprise.marketplace.categoryservice.infrastructure.persistence;

import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryPage;
import com.enterprise.marketplace.categoryservice.domain.model.CategorySearchCriteria;
import com.enterprise.marketplace.categoryservice.domain.port.CategoryRepository;
import com.enterprise.marketplace.categoryservice.infrastructure.persistence.entity.CategoryEntity;
import com.enterprise.marketplace.categoryservice.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryPersistenceMapper categoryPersistenceMapper;

    @Override
    public Category save(Category category) {
        CategoryEntity entity = category.getId() == null
                ? categoryPersistenceMapper.toEntity(category)
                : categoryJpaRepository
                        .findById(category.getId())
                        .map(existing -> {
                            categoryPersistenceMapper.updateEntity(existing, category);
                            return existing;
                        })
                        .orElseGet(() -> categoryPersistenceMapper.toEntity(category));

        return categoryPersistenceMapper.toDomain(categoryJpaRepository.save(entity));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryJpaRepository.findById(id).map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categoryJpaRepository.findBySlug(slug).map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return categoryJpaRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySlugAndIdNot(String slug, UUID id) {
        return categoryJpaRepository.existsBySlugAndIdNot(slug, id);
    }

    @Override
    public CategoryPage<Category> search(CategorySearchCriteria criteria, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<CategoryEntity> result =
                categoryJpaRepository.findAll(CategorySpecifications.fromCriteria(criteria), pageable);

        return new CategoryPage<>(
                result.getContent().stream().map(categoryPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    public void deleteById(UUID id) {
        categoryJpaRepository.deleteById(id);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.ASC, "displayOrder");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction =
                parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
