package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySku(String sku);
}
