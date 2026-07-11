package com.enterprise.marketplace.adminservice.redis;

import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import java.util.List;
import java.util.Optional;

public interface AdminCachePort {

    void cacheSetting(SettingResponse setting);

    void cacheSettings(List<SettingResponse> settings);

    void evictSetting(String settingKey);

    void evictAllSettings();

    Optional<SettingResponse> getSetting(String settingKey);

    Optional<List<SettingResponse>> getAllSettings();

    void cacheFeatureFlag(FeatureFlagResponse flag);

    void cacheFeatureFlags(List<FeatureFlagResponse> flags);

    void evictFeatureFlag(String flagKey);

    void evictAllFeatureFlags();

    Optional<FeatureFlagResponse> getFeatureFlag(String flagKey);

    Optional<List<FeatureFlagResponse>> getAllFeatureFlags();
}
