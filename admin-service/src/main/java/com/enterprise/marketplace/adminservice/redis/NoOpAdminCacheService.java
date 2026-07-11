package com.enterprise.marketplace.adminservice.redis;

import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpAdminCacheService implements AdminCachePort {

    @Override
    public void cacheSetting(SettingResponse setting) {}

    @Override
    public void cacheSettings(List<SettingResponse> settings) {}

    @Override
    public void evictSetting(String settingKey) {}

    @Override
    public void evictAllSettings() {}

    @Override
    public Optional<SettingResponse> getSetting(String settingKey) {
        return Optional.empty();
    }

    @Override
    public Optional<List<SettingResponse>> getAllSettings() {
        return Optional.empty();
    }

    @Override
    public void cacheFeatureFlag(FeatureFlagResponse flag) {}

    @Override
    public void cacheFeatureFlags(List<FeatureFlagResponse> flags) {}

    @Override
    public void evictFeatureFlag(String flagKey) {}

    @Override
    public void evictAllFeatureFlags() {}

    @Override
    public Optional<FeatureFlagResponse> getFeatureFlag(String flagKey) {
        return Optional.empty();
    }

    @Override
    public Optional<List<FeatureFlagResponse>> getAllFeatureFlags() {
        return Optional.empty();
    }
}
