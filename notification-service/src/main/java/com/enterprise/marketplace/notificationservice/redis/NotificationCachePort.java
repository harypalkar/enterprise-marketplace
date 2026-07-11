package com.enterprise.marketplace.notificationservice.redis;

import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface NotificationCachePort {

    void cacheNotification(NotificationResponse response);

    void evictNotification(UUID notificationId);

    void cacheTemplate(NotificationTemplateEntity template);

    Optional<NotificationTemplateEntity> getTemplate(String templateCode, String channel);

    void cacheChannelConfig(NotificationChannelEntity channel);

    Optional<NotificationChannelEntity> getChannelConfig(String channel);

    void incrementRateLimit(String recipientId, String channel);

    boolean isRateLimitExceeded(String recipientId, String channel, int limit);

    void cacheUserPreferences(String userId, Map<String, Object> preferences);

    Optional<Map<String, Object>> getUserPreferences(String userId);
}
