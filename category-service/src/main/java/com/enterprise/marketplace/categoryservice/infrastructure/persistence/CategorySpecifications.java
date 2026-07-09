package com.enterprise.marketplace.categoryservice.infrastructure.persistence;

import com.enterprise.marketplace.categoryservice.domain.model.CategorySearchCriteria;
import com.enterprise.marketplace.categoryservice.infrastructure.persistence.entity.CategoryEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class CategorySpecifications {

    private CategorySpecifications() {
    }

    static Specification<CategoryEntity> fromCriteria(CategorySearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getParentId() != null) {
                predicates.add(cb.equal(root.get("parentId"), criteria.getParentId()));
            }
            if (StringUtils.hasText(criteria.getKeyword())) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("slug")), pattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
