package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductEntity;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<ProductEntity> withCriteria(
            ProductStatus status, UUID sellerId, UUID categoryId, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (sellerId != null) {
                predicates.add(cb.equal(root.get("sellerId"), sellerId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("sku")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
