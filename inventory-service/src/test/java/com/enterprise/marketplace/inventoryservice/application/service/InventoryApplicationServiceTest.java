package com.enterprise.marketplace.inventoryservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.inventoryservice.application.dto.AdjustInventoryQuantityRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.CreateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.UpdateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.mapper.InventoryMapper;
import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import com.enterprise.marketplace.inventoryservice.domain.port.InventoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Spy
    private InventoryMapper inventoryMapper = Mappers.getMapper(InventoryMapper.class);

    @InjectMocks
    private InventoryApplicationService inventoryApplicationService;

    @Test
    void shouldCreateInventoryWhenUniqueCombination() {
        CreateInventoryRequest request = CreateInventoryRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .quantityAvailable(120)
                .reorderLevel(20)
                .warehouseCode("BLR-01")
                .build();

        when(inventoryRepository.existsByProductIdAndSellerIdAndWarehouseCode(
                        request.getProductId(), request.getSellerId(), request.getWarehouseCode()))
                .thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            return inventory.toBuilder().id(UUID.randomUUID()).version(0L).build();
        });

        var response = inventoryApplicationService.createInventory(request);

        assertThat(response.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void shouldThrowConflictWhenDuplicateCombinationExists() {
        CreateInventoryRequest request = CreateInventoryRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .quantityAvailable(30)
                .warehouseCode("MUM-02")
                .build();

        when(inventoryRepository.existsByProductIdAndSellerIdAndWarehouseCode(
                        request.getProductId(), request.getSellerId(), request.getWarehouseCode()))
                .thenReturn(true);

        assertThatThrownBy(() -> inventoryApplicationService.createInventory(request))
                .isInstanceOf(MarketplaceException.class)
                .extracting(ex -> ((MarketplaceException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void shouldThrowNotFoundWhenInventoryMissing() {
        UUID id = UUID.randomUUID();
        when(inventoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryApplicationService.getInventoryById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReserveAndReleaseInventory() {
        UUID id = UUID.randomUUID();
        Inventory existing = Inventory.builder()
                .id(id)
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .quantityAvailable(50)
                .quantityReserved(10)
                .reorderLevel(20)
                .warehouseCode("DEL-01")
                .status(InventoryStatus.IN_STOCK)
                .version(1L)
                .build();

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var reserveResponse = inventoryApplicationService.reserveQuantity(
                id, AdjustInventoryQuantityRequest.builder().quantity(15).build());
        assertThat(reserveResponse.getQuantityAvailable()).isEqualTo(35);
        assertThat(reserveResponse.getQuantityReserved()).isEqualTo(25);

        var releaseResponse = inventoryApplicationService.releaseQuantity(
                id, AdjustInventoryQuantityRequest.builder().quantity(5).build());
        assertThat(releaseResponse.getQuantityAvailable()).isEqualTo(55);
        assertThat(releaseResponse.getQuantityReserved()).isEqualTo(5);
    }

    @Test
    void shouldUpdateInventoryAndDeriveStatus() {
        UUID id = UUID.randomUUID();
        Inventory existing = Inventory.builder()
                .id(id)
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .quantityAvailable(100)
                .quantityReserved(0)
                .reorderLevel(10)
                .warehouseCode("HYD-01")
                .status(InventoryStatus.IN_STOCK)
                .version(1L)
                .build();

        when(inventoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateInventoryRequest request = UpdateInventoryRequest.builder()
                .quantityAvailable(5)
                .reorderLevel(15)
                .build();

        var response = inventoryApplicationService.updateInventory(id, request);
        assertThat(response.getStatus()).isEqualTo(InventoryStatus.LOW_STOCK);
    }
}
