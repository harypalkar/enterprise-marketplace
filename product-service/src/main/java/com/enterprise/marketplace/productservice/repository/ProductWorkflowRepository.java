package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductWorkflowEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductWorkflowRepository extends JpaRepository<ProductWorkflowEntity, UUID> {

    Optional<ProductWorkflowEntity> findByProductId(UUID productId);
}
