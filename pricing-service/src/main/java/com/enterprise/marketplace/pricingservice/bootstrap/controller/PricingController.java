package com.enterprise.marketplace.pricingservice.bootstrap.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.pricingservice.application.dto.CreatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.PricingPageResponse;
import com.enterprise.marketplace.pricingservice.application.dto.PricingResponse;
import com.enterprise.marketplace.pricingservice.application.dto.UpdatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.UpdatePricingStatusRequest;
import com.enterprise.marketplace.pricingservice.application.service.PricingApplicationService;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
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
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Product pricing management APIs")
public class PricingController {

    private final PricingApplicationService pricingApplicationService;

    @PostMapping
    @Idempotent
    @Operation(summary = "Create pricing entry")
    public ResponseEntity<ApiResponse<PricingResponse>> createPricing(@Valid @RequestBody CreatePricingRequest request) {
        PricingResponse response = pricingApplicationService.createPricing(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Pricing created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pricing by ID")
    public ResponseEntity<ApiResponse<PricingResponse>> getPricingById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(pricingApplicationService.getPricingById(id)));
    }

    @GetMapping
    @Operation(summary = "Search pricing by product, seller, and status")
    public ResponseEntity<ApiResponse<PricingPageResponse>> searchPricing(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) PricingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        PricingPageResponse response =
                pricingApplicationService.searchPricing(productId, sellerId, status, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update pricing entry")
    public ResponseEntity<ApiResponse<PricingResponse>> updatePricing(
            @PathVariable UUID id, @Valid @RequestBody UpdatePricingRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(pricingApplicationService.updatePricing(id, request), "Pricing updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Idempotent
    @Operation(summary = "Update pricing status")
    public ResponseEntity<ApiResponse<PricingResponse>> updatePricingStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdatePricingStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                pricingApplicationService.updatePricingStatus(id, request.getStatus()),
                "Pricing status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete pricing entry")
    public ResponseEntity<ApiResponse<Void>> deletePricing(@PathVariable UUID id) {
        pricingApplicationService.deletePricing(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Pricing deleted successfully"));
    }
}
