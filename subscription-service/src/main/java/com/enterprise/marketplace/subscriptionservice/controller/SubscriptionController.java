package com.enterprise.marketplace.subscriptionservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.subscriptionservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscribeRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionPageResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
import com.enterprise.marketplace.subscriptionservice.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription lifecycle APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Idempotent
    @Operation(
            summary = "Create subscription",
            description = "Subscribe a buyer to a seller plan. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(@Valid @RequestBody SubscribeRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subscription created successfully"));
    }

    @GetMapping("/{subscriptionId}")
    @Operation(summary = "Get subscription by ID")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getSubscription(subscriptionId)));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "List subscriptions by seller")
    public ResponseEntity<ApiResponse<SubscriptionPageResponse>> getBySeller(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getBySeller(sellerId, page, size)));
    }

    @GetMapping("/buyer/{buyerId}")
    @Operation(summary = "List subscriptions by buyer")
    public ResponseEntity<ApiResponse<SubscriptionPageResponse>> getByBuyer(
            @PathVariable UUID buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(subscriptionService.getByBuyer(buyerId, page, size)));
    }

    @PatchMapping("/{subscriptionId}/status")
    @Operation(summary = "Update subscription status")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateStatus(
            @PathVariable UUID subscriptionId, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.updateStatus(subscriptionId, request), "Subscription status updated successfully"));
    }

    @DeleteMapping("/{subscriptionId}")
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancel(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.cancel(subscriptionId), "Subscription cancelled successfully"));
    }

    @PostMapping("/{subscriptionId}/renew")
    @Operation(summary = "Renew subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renew(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.renew(subscriptionId), "Subscription renewed successfully"));
    }
}
