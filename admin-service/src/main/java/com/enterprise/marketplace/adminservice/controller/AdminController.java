package com.enterprise.marketplace.adminservice.controller;

import com.enterprise.marketplace.adminservice.dto.BulkPatchFeatureFlagsRequest;
import com.enterprise.marketplace.adminservice.dto.ConfigResponse;
import com.enterprise.marketplace.adminservice.dto.CreateConfigRequest;
import com.enterprise.marketplace.adminservice.dto.CreateSettingRequest;
import com.enterprise.marketplace.adminservice.dto.DashboardResponse;
import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.PatchFeatureFlagRequest;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import com.enterprise.marketplace.adminservice.dto.UpdateConfigRequest;
import com.enterprise.marketplace.adminservice.dto.UpdateSettingRequest;
import com.enterprise.marketplace.adminservice.service.AdminService;
import com.enterprise.marketplace.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform administration APIs (ADMIN role required)")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/settings")
    @Operation(summary = "List platform settings")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> getSettings(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSettings(category)));
    }

    @PostMapping("/settings")
    @Operation(summary = "Create platform setting")
    public ResponseEntity<ApiResponse<SettingResponse>> createSetting(
            @Valid @RequestBody CreateSettingRequest request) {
        SettingResponse response = adminService.createSetting(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Setting created successfully"));
    }

    @PutMapping("/settings/{settingKey}")
    @Operation(summary = "Update platform setting")
    public ResponseEntity<ApiResponse<SettingResponse>> updateSetting(
            @PathVariable String settingKey, @Valid @RequestBody UpdateSettingRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.updateSetting(settingKey, request), "Setting updated successfully"));
    }

    @DeleteMapping("/settings/{settingKey}")
    @Operation(summary = "Delete platform setting")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable String settingKey) {
        adminService.deleteSetting(settingKey);
        return ResponseEntity.ok(ApiResponse.success(null, "Setting deleted successfully"));
    }

    @GetMapping("/feature-flags")
    @Operation(summary = "List feature flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getFeatureFlags() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getFeatureFlags()));
    }

    @PatchMapping("/feature-flags")
    @Operation(summary = "Bulk patch feature flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> patchFeatureFlags(
            @Valid @RequestBody BulkPatchFeatureFlagsRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.patchFeatureFlags(request), "Feature flags updated successfully"));
    }

    @GetMapping("/feature-flags/{flagKey}")
    @Operation(summary = "Get feature flag by key")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getFeatureFlag(@PathVariable String flagKey) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getFeatureFlag(flagKey)));
    }

    @PatchMapping("/feature-flags/{flagKey}")
    @Operation(summary = "Patch feature flag by key")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> patchFeatureFlag(
            @PathVariable String flagKey, @Valid @RequestBody PatchFeatureFlagRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.patchFeatureFlag(flagKey, request), "Feature flag updated successfully"));
    }

    @GetMapping("/configs")
    @Operation(summary = "List admin configs")
    public ResponseEntity<ApiResponse<List<ConfigResponse>>> getConfigs(@RequestParam(required = false) String scope) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getConfigs(scope)));
    }

    @PostMapping("/configs")
    @Operation(summary = "Create admin config")
    public ResponseEntity<ApiResponse<ConfigResponse>> createConfig(@Valid @RequestBody CreateConfigRequest request) {
        ConfigResponse response = adminService.createConfig(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Config created successfully"));
    }

    @PutMapping("/configs/{configKey}")
    @Operation(summary = "Update admin config")
    public ResponseEntity<ApiResponse<ConfigResponse>> updateConfig(
            @PathVariable String configKey, @Valid @RequestBody UpdateConfigRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.updateConfig(configKey, request), "Config updated successfully"));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated platform dashboard stats")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }
}
