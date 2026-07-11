package com.enterprise.marketplace.notificationservice.redis;

import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "marketplace.redis", name = "enabled", havingValue = "false")
public class NoOpNotificationCacheService implements NotificationCachePort {

    @Override
    public void cacheNotification(NotificationResponse response) {
        // no-op when redis disabled
    }

    @Override
    public void evictNotification(UUID notificationId) {
        // no-op when redis disabled
    }

    @Override
    public void cacheTemplate(NotificationTemplateEntity template) {
        // no-op when redis disabled
    }

    @Override
    public Optional<NotificationTemplateEntity> getTemplate(String templateCode, String channel) {
        return Optional.empty();
    }

    @Override
    public void cacheChannelConfig(NotificationChannelEntity channel) {
        // no-op when redis disabled
    }

    @Override
    public Optional<NotificationChannelEntity> getChannelConfig(String channel) {
        return Optional.empty();
    }

    @Override
    public void incrementRateLimit(String recipientId, String channel) {
        // no-op when redis disabled
    }

    @Override
    public boolean isRateLimitExceeded(String recipientId, String channel, int limit) {
        return false;
    }

    @Override
    public void cacheUserPreferences(String userId, Map<String, Object> preferences) {
        // no-op when redis disabled
    }

    @Override
    public Optional<Map<String, Object>> getUserPreferences(String userId) {
        return Optional.empty();
    }
}
