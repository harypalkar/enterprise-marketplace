package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductInventoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductInventoryRepository extends JpaRepository<ProductInventoryEntity, UUID> {

    Optional<ProductInventoryEntity> findByProductId(UUID productId);
}
