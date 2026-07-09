package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductImageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, UUID> {

    List<ProductImageEntity> findByProductIdOrderByDisplayOrderAsc(UUID productId);

    void deleteByProductId(UUID productId);
}
