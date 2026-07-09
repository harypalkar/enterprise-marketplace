package com.enterprise.marketplace.inventoryservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.inventoryservice.application.dto.AdjustInventoryQuantityRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.CreateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryPageResponse;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryResponse;
import com.enterprise.marketplace.inventoryservice.application.dto.UpdateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.mapper.InventoryMapper;
import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryPage;
import com.enterprise.marketplace.inventoryservice.domain.model.InventorySearchCriteria;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import com.enterprise.marketplace.inventoryservice.domain.port.InventoryRepository;
import com.enterprise.marketplace.inventoryservice.domain.service.InventoryDomainService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryApplicationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        if (inventoryRepository.existsByProductIdAndSellerIdAndWarehouseCode(
                request.getProductId(), request.getSellerId(), request.getWarehouseCode())) {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT,
                    "Inventory already exists for product, seller and warehouse combination");
        }

        Inventory inventory = inventoryMapper.toDomain(request);
        Inventory normalized = inventory.withStatus(deriveStatus(inventory));
        validateForCreate(normalized);
        return inventoryMapper.toResponse(inventoryRepository.save(normalized));
    }

    public InventoryResponse getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
        return inventoryMapper.toResponse(inventory);
    }

    public InventoryPageResponse searchInventory(
            UUID productId, UUID sellerId, InventoryStatus status, int page, int size, String sort) {
        InventorySearchCriteria criteria = InventorySearchCriteria.builder()
                .productId(productId)
                .sellerId(sellerId)
                .status(status)
                .build();

        InventoryPage<Inventory> result = inventoryRepository.search(criteria, page, size, sort);
        List<InventoryResponse> content =
                result.content().stream().map(inventoryMapper::toResponse).toList();
        return InventoryPageResponse.builder()
                .content(content)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .page(result.pageNumber())
                .size(result.pageSize())
                .build();
    }

    @Transactional
    public InventoryResponse updateInventory(UUID id, UpdateInventoryRequest request) {
        Inventory existing = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));

        Inventory updated = existing.toBuilder()
                .quantityAvailable(
                        request.getQuantityAvailable() != null
                                ? request.getQuantityAvailable()
                                : existing.getQuantityAvailable())
                .quantityReserved(
                        request.getQuantityReserved() != null
                                ? request.getQuantityReserved()
                                : existing.getQuantityReserved())
                .reorderLevel(
                        request.getReorderLevel() != null
                                ? request.getReorderLevel()
                                : existing.getReorderLevel())
                .warehouseCode(
                        request.getWarehouseCode() != null
                                ? request.getWarehouseCode()
                                : existing.getWarehouseCode())
                .status(request.getStatus() != null ? request.getStatus() : deriveStatus(existing))
                .build();

        Inventory normalized = updated.withStatus(
                request.getStatus() != null ? request.getStatus() : deriveStatus(updated));
        validateForUpdate(normalized);
        return inventoryMapper.toResponse(inventoryRepository.save(normalized));
    }

    @Transactional
    public InventoryResponse reserveQuantity(UUID id, AdjustInventoryQuantityRequest request) {
        Inventory existing = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
        int quantity = request.getQuantity();
        if (existing.getQuantityAvailable() < quantity) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION, "Insufficient available inventory to reserve");
        }

        Inventory updated = existing.toBuilder()
                .quantityAvailable(existing.getQuantityAvailable() - quantity)
                .quantityReserved(existing.getQuantityReserved() + quantity)
                .build();
        Inventory normalized = updated.withStatus(deriveStatus(updated));
        validateForUpdate(normalized);
        return inventoryMapper.toResponse(inventoryRepository.save(normalized));
    }

    @Transactional
    public InventoryResponse releaseQuantity(UUID id, AdjustInventoryQuantityRequest request) {
        Inventory existing = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
        int quantity = request.getQuantity();
        if (existing.getQuantityReserved() < quantity) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION, "Release quantity cannot exceed reserved inventory");
        }

        Inventory updated = existing.toBuilder()
                .quantityAvailable(existing.getQuantityAvailable() + quantity)
                .quantityReserved(existing.getQuantityReserved() - quantity)
                .build();
        Inventory normalized = updated.withStatus(deriveStatus(updated));
        validateForUpdate(normalized);
        return inventoryMapper.toResponse(inventoryRepository.save(normalized));
    }

    @Transactional
    public void deleteInventory(UUID id) {
        Inventory existing = inventoryRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));
        inventoryRepository.deleteById(existing.getId());
    }

    private void validateForCreate(Inventory inventory) {
        try {
            InventoryDomainService.validateForCreate(inventory);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
    }

    private void validateForUpdate(Inventory inventory) {
        try {
            InventoryDomainService.validateForUpdate(inventory);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
    }

    private InventoryStatus deriveStatus(Inventory inventory) {
        return InventoryDomainService.deriveStatus(
                inventory.getQuantityAvailable(), inventory.getReorderLevel());
    }
}
