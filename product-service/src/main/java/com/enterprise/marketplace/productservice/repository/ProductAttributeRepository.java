package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductAttributeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttributeEntity, UUID> {

    List<ProductAttributeEntity> findByProductId(UUID productId);

    void deleteByProductId(UUID productId);
}
