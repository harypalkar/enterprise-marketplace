package com.enterprise.marketplace.notificationservice.redis;

import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
import java.util.Optional;
import java.util.UUID;

public interface NotificationCachePort {

    void cacheNotification(NotificationResponse response);

    void evictNotification(UUID notificationId);

    void cacheTemplate(NotificationTemplateEntity template);

    Optional<NotificationTemplateEntity> getTemplate(String templateCode, String channel);
}
