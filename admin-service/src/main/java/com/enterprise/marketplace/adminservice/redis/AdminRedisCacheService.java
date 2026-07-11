package com.enterprise.marketplace.adminservice.redis;

import com.enterprise.marketplace.adminservice.constants.AdminCacheKeys;
import com.enterprise.marketplace.adminservice.dto.FeatureFlagResponse;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AdminRedisCacheService implements AdminCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.admin-cache-ttl-seconds:3600}")
    private long cacheTtlSeconds;

    @Override
    public void cacheSetting(SettingResponse setting) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            AdminCacheKeys.settingKey(setting.getSettingKey()),
                            objectMapper.writeValueAsString(setting),
                            Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache setting key={}", setting.getSettingKey(), ex);
        }
    }

    @Override
    public void cacheSettings(List<SettingResponse> settings) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            AdminCacheKeys.settingsAllKey(),
                            objectMapper.writeValueAsString(settings),
                            Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache all settings", ex);
        }
    }

    @Override
    public void evictSetting(String settingKey) {
        redisTemplate.delete(AdminCacheKeys.settingKey(settingKey));
        redisTemplate.delete(AdminCacheKeys.settingsAllKey());
    }

    @Override
    public void evictAllSettings() {
        redisTemplate.delete(AdminCacheKeys.settingsAllKey());
    }

    @Override
    public Optional<SettingResponse> getSetting(String settingKey) {
        try {
            String cached = redisTemplate.opsForValue().get(AdminCacheKeys.settingKey(settingKey));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, SettingResponse.class));
        } catch (Exception ex) {
            log.debug("Setting cache miss for key={}", settingKey, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<SettingResponse>> getAllSettings() {
        try {
            String cached = redisTemplate.opsForValue().get(AdminCacheKeys.settingsAllKey());
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, new TypeReference<>() {}));
        } catch (Exception ex) {
            log.debug("Settings list cache miss", ex);
            return Optional.empty();
        }
    }

    @Override
    public void cacheFeatureFlag(FeatureFlagResponse flag) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            AdminCacheKeys.featureFlagKey(flag.getFlagKey()),
                            objectMapper.writeValueAsString(flag),
                            Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache feature flag key={}", flag.getFlagKey(), ex);
        }
    }

    @Override
    public void cacheFeatureFlags(List<FeatureFlagResponse> flags) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            AdminCacheKeys.featureFlagsAllKey(),
                            objectMapper.writeValueAsString(flags),
                            Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache all feature flags", ex);
        }
    }

    @Override
    public void evictFeatureFlag(String flagKey) {
        redisTemplate.delete(AdminCacheKeys.featureFlagKey(flagKey));
        redisTemplate.delete(AdminCacheKeys.featureFlagsAllKey());
    }

    @Override
    public void evictAllFeatureFlags() {
        redisTemplate.delete(AdminCacheKeys.featureFlagsAllKey());
    }

    @Override
    public Optional<FeatureFlagResponse> getFeatureFlag(String flagKey) {
        try {
            String cached = redisTemplate.opsForValue().get(AdminCacheKeys.featureFlagKey(flagKey));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, FeatureFlagResponse.class));
        } catch (Exception ex) {
            log.debug("Feature flag cache miss for key={}", flagKey, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<FeatureFlagResponse>> getAllFeatureFlags() {
        try {
            String cached = redisTemplate.opsForValue().get(AdminCacheKeys.featureFlagsAllKey());
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, new TypeReference<>() {}));
        } catch (Exception ex) {
            log.debug("Feature flags list cache miss", ex);
            return Optional.empty();
        }
    }
}
