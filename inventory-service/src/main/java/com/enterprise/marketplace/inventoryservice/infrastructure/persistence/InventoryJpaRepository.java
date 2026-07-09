package com.enterprise.marketplace.inventoryservice.infrastructure.persistence;

import com.enterprise.marketplace.inventoryservice.infrastructure.persistence.entity.InventoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryJpaRepository
        extends JpaRepository<InventoryEntity, UUID>, JpaSpecificationExecutor<InventoryEntity> {

    boolean existsByProductIdAndSellerIdAndWarehouseCode(UUID productId, UUID sellerId, String warehouseCode);
}
