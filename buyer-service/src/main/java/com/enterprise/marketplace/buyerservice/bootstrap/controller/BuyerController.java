package com.enterprise.marketplace.buyerservice.bootstrap.controller;

import com.enterprise.marketplace.buyerservice.application.dto.BuyerPageResponse;
import com.enterprise.marketplace.buyerservice.application.dto.BuyerResponse;
import com.enterprise.marketplace.buyerservice.application.dto.CreateBuyerRequest;
import com.enterprise.marketplace.buyerservice.application.dto.UpdateBuyerRequest;
import com.enterprise.marketplace.buyerservice.application.dto.UpdateBuyerStatusRequest;
import com.enterprise.marketplace.buyerservice.application.service.BuyerApplicationService;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.idempotency.Idempotent;
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
@RequestMapping("/api/v1/buyers")
@RequiredArgsConstructor
@Tag(name = "Buyers", description = "Buyer management APIs")
public class BuyerController {

    private final BuyerApplicationService buyerApplicationService;

    @PostMapping
    @Idempotent
    @Operation(summary = "Create a buyer")
    public ResponseEntity<ApiResponse<BuyerResponse>> createBuyer(@Valid @RequestBody CreateBuyerRequest request) {
        BuyerResponse response = buyerApplicationService.createBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Buyer created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get buyer by ID")
    public ResponseEntity<ApiResponse<BuyerResponse>> getBuyerById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(buyerApplicationService.getBuyerById(id)));
    }

    @GetMapping
    @Operation(summary = "Search buyers with pagination")
    public ResponseEntity<ApiResponse<BuyerPageResponse>> searchBuyers(
            @RequestParam(required = false) BuyerStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        BuyerPageResponse response =
                buyerApplicationService.searchBuyers(status, city, state, country, keyword, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update a buyer")
    public ResponseEntity<ApiResponse<BuyerResponse>> updateBuyer(
            @PathVariable UUID id, @Valid @RequestBody UpdateBuyerRequest request) {
        BuyerResponse response = buyerApplicationService.updateBuyer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Buyer updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Idempotent
    @Operation(summary = "Update buyer status")
    public ResponseEntity<ApiResponse<BuyerResponse>> updateBuyerStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateBuyerStatusRequest request) {
        BuyerResponse response = buyerApplicationService.updateBuyerStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Buyer status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archive a buyer")
    public ResponseEntity<ApiResponse<Void>> archiveBuyer(@PathVariable UUID id) {
        buyerApplicationService.archiveBuyer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Buyer archived successfully"));
    }
}
