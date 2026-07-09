package com.enterprise.marketplace.sellerservice.infrastructure.persistence;

import com.enterprise.marketplace.sellerservice.domain.model.SellerSearchCriteria;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import com.enterprise.marketplace.sellerservice.infrastructure.persistence.entity.SellerEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class SellerSpecifications {

    private SellerSpecifications() {
    }

    static Specification<SellerEntity> fromCriteria(SellerSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            } else {
                predicates.add(cb.notEqual(root.get("status"), SellerStatus.ARCHIVED));
            }

            if (StringUtils.hasText(criteria.getKeyword())) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("companyName")), pattern),
                        cb.like(cb.lower(root.get("tradeName")), pattern),
                        cb.like(cb.lower(root.get("gstin")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
