package com.enterprise.marketplace.adminservice.service;

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
import java.util.List;

public interface AdminService {

    List<SettingResponse> getSettings(String category);

    SettingResponse createSetting(CreateSettingRequest request);

    SettingResponse updateSetting(String settingKey, UpdateSettingRequest request);

    void deleteSetting(String settingKey);

    List<FeatureFlagResponse> getFeatureFlags();

    FeatureFlagResponse getFeatureFlag(String flagKey);

    List<FeatureFlagResponse> patchFeatureFlags(BulkPatchFeatureFlagsRequest request);

    FeatureFlagResponse patchFeatureFlag(String flagKey, PatchFeatureFlagRequest request);

    List<ConfigResponse> getConfigs(String scope);

    ConfigResponse createConfig(CreateConfigRequest request);

    ConfigResponse updateConfig(String configKey, UpdateConfigRequest request);

    DashboardResponse getDashboard();
}
