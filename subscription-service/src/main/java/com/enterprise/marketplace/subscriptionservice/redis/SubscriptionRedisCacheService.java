package com.enterprise.marketplace.subscriptionservice.redis;

import com.enterprise.marketplace.subscriptionservice.constants.SubscriptionCacheKeys;
import com.enterprise.marketplace.subscriptionservice.dto.PlanResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
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
public class SubscriptionRedisCacheService implements SubscriptionCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.subscription-cache-ttl-seconds:3600}")
    private long subscriptionCacheTtlSeconds;

    @Value("${marketplace.redis.plan-cache-ttl-seconds:7200}")
    private long planCacheTtlSeconds;

    @Override
    public void cacheSubscription(SubscriptionResponse response) {
        try {
            String key = SubscriptionCacheKeys.subscriptionKey(response.getId());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(response), Duration.ofSeconds(subscriptionCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache subscription id={}", response.getId(), ex);
        }
    }

    @Override
    public void evictSubscription(UUID subscriptionId) {
        redisTemplate.delete(SubscriptionCacheKeys.subscriptionKey(subscriptionId));
    }

    @Override
    public Optional<SubscriptionResponse> getSubscription(UUID subscriptionId) {
        try {
            String cached = redisTemplate.opsForValue().get(SubscriptionCacheKeys.subscriptionKey(subscriptionId));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, SubscriptionResponse.class));
        } catch (Exception ex) {
            log.debug("Subscription cache miss or parse error for id={}", subscriptionId, ex);
            return Optional.empty();
        }
    }

    @Override
    public void cachePlan(PlanResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate
                    .opsForValue()
                    .set(SubscriptionCacheKeys.planKey(response.getId()), json, Duration.ofSeconds(planCacheTtlSeconds));
            redisTemplate
                    .opsForValue()
                    .set(
                            SubscriptionCacheKeys.planCodeKey(response.getPlanCode()),
                            json,
                            Duration.ofSeconds(planCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache plan id={}", response.getId(), ex);
        }
    }

    @Override
    public void evictPlan(UUID planId, String planCode) {
        redisTemplate.delete(SubscriptionCacheKeys.planKey(planId));
        if (planCode != null) {
            redisTemplate.delete(SubscriptionCacheKeys.planCodeKey(planCode));
        }
    }

    @Override
    public Optional<PlanResponse> getPlan(UUID planId) {
        try {
            String cached = redisTemplate.opsForValue().get(SubscriptionCacheKeys.planKey(planId));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, PlanResponse.class));
        } catch (Exception ex) {
            log.debug("Plan cache miss or parse error for id={}", planId, ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PlanResponse> getPlanByCode(String planCode) {
        try {
            String cached = redisTemplate.opsForValue().get(SubscriptionCacheKeys.planCodeKey(planCode));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, PlanResponse.class));
        } catch (Exception ex) {
            log.debug("Plan cache miss or parse error for planCode={}", planCode, ex);
            return Optional.empty();
        }
    }
}
