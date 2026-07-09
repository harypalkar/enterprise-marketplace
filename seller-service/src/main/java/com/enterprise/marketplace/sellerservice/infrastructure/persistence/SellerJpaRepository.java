package com.enterprise.marketplace.sellerservice.infrastructure.persistence;

import com.enterprise.marketplace.sellerservice.infrastructure.persistence.entity.SellerEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SellerJpaRepository extends JpaRepository<SellerEntity, UUID>, JpaSpecificationExecutor<SellerEntity> {

    Optional<SellerEntity> findByGstin(String gstin);

    boolean existsByGstin(String gstin);

    boolean existsByGstinAndIdNot(String gstin, UUID id);
}
