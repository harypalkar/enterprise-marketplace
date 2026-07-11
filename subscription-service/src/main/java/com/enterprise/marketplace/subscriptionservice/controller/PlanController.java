package com.enterprise.marketplace.subscriptionservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.subscriptionservice.dto.CreatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.dto.PlanListResponse;
import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.UpdatePlanRequest;
import com.enterprise.marketplace.subscriptionservice.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plans", description = "Subscription plan catalog APIs")
public class PlanController {

    private final SubscriptionPlanService planService;

    @PostMapping
    @Operation(summary = "Create subscription plan (ADMIN)")
    public ResponseEntity<ApiResponse<PlanResponse>> createPlan(@Valid @RequestBody CreatePlanRequest request) {
        PlanResponse response = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Subscription plan created successfully"));
    }

    @GetMapping
    @Operation(summary = "List active subscription plans (public)")
    public ResponseEntity<ApiResponse<PlanListResponse>> listPlans() {
        return ResponseEntity.ok(ApiResponse.success(planService.listPlans()));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get subscription plan by ID (public)")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlan(@PathVariable UUID planId) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlan(planId)));
    }

    @GetMapping("/code/{planCode}")
    @Operation(summary = "Get subscription plan by code (public)")
    public ResponseEntity<ApiResponse<PlanResponse>> getPlanByCode(@PathVariable String planCode) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanByCode(planCode)));
    }

    @PutMapping("/{planId}")
    @Operation(summary = "Update subscription plan (ADMIN)")
    public ResponseEntity<ApiResponse<PlanResponse>> updatePlan(
            @PathVariable UUID planId, @Valid @RequestBody UpdatePlanRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(planService.updatePlan(planId, request), "Subscription plan updated successfully"));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "Deactivate subscription plan (ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable UUID planId) {
        planService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription plan deactivated successfully"));
    }
}
