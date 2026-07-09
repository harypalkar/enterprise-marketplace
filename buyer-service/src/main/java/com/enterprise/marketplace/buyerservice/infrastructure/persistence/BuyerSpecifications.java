package com.enterprise.marketplace.buyerservice.infrastructure.persistence;

import com.enterprise.marketplace.buyerservice.domain.model.BuyerSearchCriteria;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import com.enterprise.marketplace.buyerservice.infrastructure.persistence.entity.BuyerEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

final class BuyerSpecifications {

    private BuyerSpecifications() {
    }

    static Specification<BuyerEntity> fromCriteria(BuyerSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            } else {
                predicates.add(cb.notEqual(root.get("status"), BuyerStatus.ARCHIVED));
            }
            if (StringUtils.hasText(criteria.getCity())) {
                predicates.add(cb.equal(cb.lower(root.get("city")), criteria.getCity().toLowerCase()));
            }
            if (StringUtils.hasText(criteria.getState())) {
                predicates.add(cb.equal(cb.lower(root.get("state")), criteria.getState().toLowerCase()));
            }
            if (StringUtils.hasText(criteria.getCountry())) {
                predicates.add(cb.equal(cb.lower(root.get("country")), criteria.getCountry().toLowerCase()));
            }
            if (StringUtils.hasText(criteria.getKeyword())) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("companyName")), pattern),
                        cb.like(cb.lower(root.get("contactPerson")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
