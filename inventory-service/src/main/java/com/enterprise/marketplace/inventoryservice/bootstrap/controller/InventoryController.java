package com.enterprise.marketplace.inventoryservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.inventoryservice.application.dto.AdjustInventoryQuantityRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.CreateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryPageResponse;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryResponse;
import com.enterprise.marketplace.inventoryservice.application.dto.UpdateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.service.InventoryApplicationService;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    @PostMapping
    @Idempotent
    @Operation(summary = "Create inventory record")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryApplicationService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Inventory created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryApplicationService.getInventoryById(id)));
    }

    @GetMapping
    @Operation(summary = "Search inventory with pagination")
    public ResponseEntity<ApiResponse<InventoryPageResponse>> searchInventory(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) InventoryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        InventoryPageResponse response =
                inventoryApplicationService.searchInventory(productId, sellerId, status, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update inventory record")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable UUID id, @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryApplicationService.updateInventory(id, request), "Inventory updated successfully"));
    }

    @PatchMapping("/{id}/reserve")
    @Idempotent
    @Operation(summary = "Reserve inventory quantity")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveQuantity(
            @PathVariable UUID id, @Valid @RequestBody AdjustInventoryQuantityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryApplicationService.reserveQuantity(id, request), "Inventory reserved successfully"));
    }

    @PatchMapping("/{id}/release")
    @Idempotent
    @Operation(summary = "Release reserved inventory quantity")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseQuantity(
            @PathVariable UUID id, @Valid @RequestBody AdjustInventoryQuantityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryApplicationService.releaseQuantity(id, request), "Inventory released successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inventory record")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable UUID id) {
        inventoryApplicationService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Inventory deleted successfully"));
    }
}
