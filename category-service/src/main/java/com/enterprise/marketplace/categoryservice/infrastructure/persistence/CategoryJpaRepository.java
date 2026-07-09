package com.enterprise.marketplace.categoryservice.infrastructure.persistence;

import com.enterprise.marketplace.categoryservice.infrastructure.persistence.entity.CategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryJpaRepository
        extends JpaRepository<CategoryEntity, UUID>, JpaSpecificationExecutor<CategoryEntity> {

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
