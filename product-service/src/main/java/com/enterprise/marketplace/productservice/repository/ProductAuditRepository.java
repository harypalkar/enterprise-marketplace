package com.enterprise.marketplace.productservice.repository;

import com.enterprise.marketplace.productservice.entity.ProductAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAuditRepository extends JpaRepository<ProductAuditEntity, UUID> {
}
