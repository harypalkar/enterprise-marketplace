package com.enterprise.marketplace.pricingservice.infrastructure.persistence;

import com.enterprise.marketplace.pricingservice.domain.model.PricingSearchCriteria;
import com.enterprise.marketplace.pricingservice.infrastructure.persistence.entity.PricingEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

final class PricingSpecifications {

    private PricingSpecifications() {
    }

    static Specification<PricingEntity> fromCriteria(PricingSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getProductId() != null) {
                predicates.add(cb.equal(root.get("productId"), criteria.getProductId()));
            }
            if (criteria.getSellerId() != null) {
                predicates.add(cb.equal(root.get("sellerId"), criteria.getSellerId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
