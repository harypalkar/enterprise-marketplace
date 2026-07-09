package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductPriceEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceRepository extends JpaRepository<ProductPriceEntity, UUID> {

    java.util.Optional<ProductPriceEntity> findFirstByProductIdOrderByValidFromDesc(UUID productId);

    void deleteByProductId(UUID productId);
}
