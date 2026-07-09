package com.enterprise.marketplace.sellerservice.domain.port;

import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerPage;
import com.enterprise.marketplace.sellerservice.domain.model.SellerSearchCriteria;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for seller persistence.
 */
public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(UUID id);

    Optional<Seller> findByGstin(String gstin);

    boolean existsByGstin(String gstin);

    boolean existsByGstinAndIdNot(String gstin, UUID id);

    SellerPage<Seller> search(SellerSearchCriteria criteria, int page, int size, String sort);

    void deleteById(UUID id);
}
