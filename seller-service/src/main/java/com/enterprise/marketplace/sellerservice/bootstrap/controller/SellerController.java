package com.enterprise.marketplace.sellerservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.sellerservice.application.dto.CreateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.SellerPageResponse;
import com.enterprise.marketplace.sellerservice.application.dto.SellerResponse;
import com.enterprise.marketplace.sellerservice.application.dto.UpdateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.UpdateSellerStatusRequest;
import com.enterprise.marketplace.sellerservice.application.service.SellerApplicationService;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
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
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
@Tag(name = "Sellers", description = "Seller management APIs")
public class SellerController {

    private final SellerApplicationService sellerApplicationService;

    @PostMapping
    @Idempotent
    @Operation(summary = "Create a seller")
    public ResponseEntity<ApiResponse<SellerResponse>> createSeller(@Valid @RequestBody CreateSellerRequest request) {
        SellerResponse response = sellerApplicationService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Seller created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seller by ID")
    public ResponseEntity<ApiResponse<SellerResponse>> getSellerById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sellerApplicationService.getSellerById(id)));
    }

    @GetMapping
    @Operation(summary = "Search sellers with pagination")
    public ResponseEntity<ApiResponse<SellerPageResponse>> searchSellers(
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        SellerPageResponse response = sellerApplicationService.searchSellers(status, keyword, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update a seller")
    public ResponseEntity<ApiResponse<SellerResponse>> updateSeller(
            @PathVariable UUID id, @Valid @RequestBody UpdateSellerRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(sellerApplicationService.updateSeller(id, request), "Seller updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Idempotent
    @Operation(summary = "Update seller status")
    public ResponseEntity<ApiResponse<SellerResponse>> updateSellerStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateSellerStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerApplicationService.updateSellerStatus(id, request.getStatus()),
                "Seller status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a seller")
    public ResponseEntity<ApiResponse<Void>> archiveSeller(@PathVariable UUID id) {
        sellerApplicationService.archiveSeller(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Seller archived successfully"));
    }
}
