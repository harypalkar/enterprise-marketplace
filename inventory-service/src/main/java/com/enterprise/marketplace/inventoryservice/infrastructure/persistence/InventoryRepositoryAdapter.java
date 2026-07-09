package com.enterprise.marketplace.inventoryservice.infrastructure.persistence;

import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryPage;
import com.enterprise.marketplace.inventoryservice.domain.model.InventorySearchCriteria;
import com.enterprise.marketplace.inventoryservice.domain.port.InventoryRepository;
import com.enterprise.marketplace.inventoryservice.infrastructure.persistence.entity.InventoryEntity;
import com.enterprise.marketplace.inventoryservice.infrastructure.persistence.mapper.InventoryPersistenceMapper;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;
    private final InventoryPersistenceMapper inventoryPersistenceMapper;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryEntity entity = inventory.getId() == null
                ? inventoryPersistenceMapper.toEntity(inventory)
                : inventoryJpaRepository
                        .findById(inventory.getId())
                        .map(existing -> {
                            inventoryPersistenceMapper.updateEntity(existing, inventory);
                            return existing;
                        })
                        .orElseGet(() -> inventoryPersistenceMapper.toEntity(inventory));
        return inventoryPersistenceMapper.toDomain(inventoryJpaRepository.save(entity));
    }

    @Override
    public Optional<Inventory> findById(UUID id) {
        return inventoryJpaRepository.findById(id).map(inventoryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByProductIdAndSellerIdAndWarehouseCode(
            UUID productId, UUID sellerId, String warehouseCode) {
        return inventoryJpaRepository.existsByProductIdAndSellerIdAndWarehouseCode(
                productId, sellerId, warehouseCode);
    }

    @Override
    public InventoryPage<Inventory> search(InventorySearchCriteria criteria, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<InventoryEntity> result =
                inventoryJpaRepository.findAll(InventorySpecifications.fromCriteria(criteria), pageable);
        return new InventoryPage<>(
                result.getContent().stream().map(inventoryPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Override
    public void deleteById(UUID id) {
        inventoryJpaRepository.deleteById(id);
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction =
                parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
