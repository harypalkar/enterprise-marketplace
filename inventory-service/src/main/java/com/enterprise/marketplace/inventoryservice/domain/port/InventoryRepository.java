package com.enterprise.marketplace.inventoryservice.domain.port;

import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryPage;
import com.enterprise.marketplace.inventoryservice.domain.model.InventorySearchCriteria;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(UUID id);

    boolean existsByProductIdAndSellerIdAndWarehouseCode(UUID productId, UUID sellerId, String warehouseCode);

    InventoryPage<Inventory> search(InventorySearchCriteria criteria, int page, int size, String sort);

    void deleteById(UUID id);
}
