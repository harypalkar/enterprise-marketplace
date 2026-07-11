package com.enterprise.marketplace.notificationservice.redis;

import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationTemplateEntity;
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
}
