package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductDocumentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductDocumentRepository extends JpaRepository<ProductDocumentEntity, UUID> {

    List<ProductDocumentEntity> findByProductId(UUID productId);
}
