package com.enterprise.marketplace.pricingservice.domain.port;

import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingPage;
import com.enterprise.marketplace.pricingservice.domain.model.PricingSearchCriteria;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for pricing persistence.
 */
public interface PricingRepository {

    Pricing save(Pricing pricing);

    Optional<Pricing> findById(UUID id);

    PricingPage<Pricing> search(PricingSearchCriteria criteria, int page, int size, String sort);

    void deleteById(UUID id);
}
