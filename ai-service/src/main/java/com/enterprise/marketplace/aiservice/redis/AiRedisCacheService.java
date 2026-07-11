package com.enterprise.marketplace.aiservice.redis;

import com.enterprise.marketplace.aiservice.constants.AiCacheKeys;
import java.time.Duration;
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
public class AiRedisCacheService implements AiCachePort {

    private final StringRedisTemplate redisTemplate;

    @Value("${marketplace.redis.prompt-cache-ttl-seconds:3600}")
    private long promptCacheTtlSeconds;

    @Value("${marketplace.redis.session-cache-ttl-seconds:1800}")
    private long productCacheTtlSeconds;

    @Override
    public void cachePromptTemplate(String templateCode, String json) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(AiCacheKeys.PROMPT_PREFIX + templateCode, json, Duration.ofSeconds(promptCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache prompt template code={}", templateCode, ex);
        }
    }

    @Override
    public Optional<String> getPromptTemplate(String templateCode) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(AiCacheKeys.PROMPT_PREFIX + templateCode));
        } catch (Exception ex) {
            log.debug("Prompt cache miss code={}", templateCode, ex);
            return Optional.empty();
        }
    }

    @Override
    public void cacheProductSnapshot(String productId, String json) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(AiCacheKeys.PRODUCT_PREFIX + productId, json, Duration.ofSeconds(productCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache product snapshot id={}", productId, ex);
        }
    }

    @Override
    public Optional<String> getProductSnapshot(String productId) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(AiCacheKeys.PRODUCT_PREFIX + productId));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public void setFeatureEnabled(boolean enabled) {
        try {
            redisTemplate.opsForValue().set(AiCacheKeys.FEATURE_ENABLED, Boolean.toString(enabled));
        } catch (Exception ex) {
            log.warn("Failed to cache AI feature flag", ex);
        }
    }

    @Override
    public Optional<Boolean> getFeatureEnabled() {
        try {
            String value = redisTemplate.opsForValue().get(AiCacheKeys.FEATURE_ENABLED);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(Boolean.parseBoolean(value));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
