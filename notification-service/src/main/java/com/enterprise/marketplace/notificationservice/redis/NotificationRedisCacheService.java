package com.enterprise.marketplace.notificationservice.redis;

import com.enterprise.marketplace.notificationservice.constants.NotificationCacheKeys;
import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
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
public class NotificationRedisCacheService implements NotificationCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.redis.template-cache-ttl-seconds:3600}")
    private long templateCacheTtlSeconds;

    @Value("${marketplace.redis.notification-cache-ttl-seconds:1800}")
    private long notificationCacheTtlSeconds;

    @Value("${marketplace.redis.channel-cache-ttl-seconds:3600}")
    private long channelCacheTtlSeconds;

    @Value("${marketplace.redis.preference-cache-ttl-seconds:86400}")
    private long preferenceCacheTtlSeconds;

    @Override
    public void cacheNotification(NotificationResponse response) {
        try {
            String key = NotificationCacheKeys.notificationKey(response.getId());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(response), Duration.ofSeconds(notificationCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache notification id={}", response.getId(), ex);
        }
    }

    @Override
    public void evictNotification(UUID notificationId) {
        redisTemplate.delete(NotificationCacheKeys.notificationKey(notificationId));
    }

    @Override
    public void cacheTemplate(NotificationTemplateEntity template) {
        try {
            String key = NotificationCacheKeys.templateKey(template.getTemplateCode(), template.getChannel().name());
            redisTemplate
                    .opsForValue()
                    .set(key, objectMapper.writeValueAsString(template), Duration.ofSeconds(templateCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache template code={}", template.getTemplateCode(), ex);
        }
    }

    @Override
    public Optional<NotificationTemplateEntity> getTemplate(String templateCode, String channel) {
        try {
            String cached =
                    redisTemplate.opsForValue().get(NotificationCacheKeys.templateKey(templateCode, channel));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, NotificationTemplateEntity.class));
        } catch (Exception ex) {
            log.debug("Template cache miss or parse error for code={}", templateCode, ex);
            return Optional.empty();
        }
    }

    @Override
    public void cacheChannelConfig(NotificationChannelEntity channel) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            NotificationCacheKeys.channelKey(channel.getChannel().name()),
                            objectMapper.writeValueAsString(channel),
                            Duration.ofSeconds(channelCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache channel config channel={}", channel.getChannel(), ex);
        }
    }

    @Override
    public Optional<NotificationChannelEntity> getChannelConfig(String channel) {
        try {
            String cached = redisTemplate.opsForValue().get(NotificationCacheKeys.channelKey(channel));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, NotificationChannelEntity.class));
        } catch (Exception ex) {
            log.debug("Channel cache miss for channel={}", channel, ex);
            return Optional.empty();
        }
    }

    @Override
    public void incrementRateLimit(String recipientId, String channel) {
        String key = NotificationCacheKeys.rateLimitKey(recipientId, channel);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofHours(1));
        }
    }

    @Override
    public boolean isRateLimitExceeded(String recipientId, String channel, int limit) {
        String key = NotificationCacheKeys.rateLimitKey(recipientId, channel);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            return Long.parseLong(value) >= limit;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public void cacheUserPreferences(String userId, Map<String, Object> preferences) {
        try {
            redisTemplate
                    .opsForValue()
                    .set(
                            NotificationCacheKeys.userPreferenceKey(userId),
                            objectMapper.writeValueAsString(preferences),
                            Duration.ofSeconds(preferenceCacheTtlSeconds));
        } catch (Exception ex) {
            log.warn("Failed to cache user preferences userId={}", userId, ex);
        }
    }

    @Override
    public Optional<Map<String, Object>> getUserPreferences(String userId) {
        try {
            String cached = redisTemplate.opsForValue().get(NotificationCacheKeys.userPreferenceKey(userId));
            if (cached == null || cached.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(cached, new TypeReference<>() {}));
        } catch (Exception ex) {
            log.debug("User preference cache miss userId={}", userId, ex);
            return Optional.empty();
        }
    }
}
