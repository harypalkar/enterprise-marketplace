package com.enterprise.marketplace.notificationservice.service;

import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.redis.NotificationCachePort;
import com.enterprise.marketplace.notificationservice.repository.NotificationChannelRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationChannelConfigService {

    private final NotificationChannelRepository channelRepository;
    private final NotificationCachePort cachePort;

    @Transactional(readOnly = true)
    public Optional<NotificationChannelEntity> getEnabledChannel(NotificationChannel channel) {
        return cachePort
                .getChannelConfig(channel.name())
                .or(() -> channelRepository.findByChannelAndEnabledTrue(channel).map(entity -> {
                    cachePort.cacheChannelConfig(entity);
                    return entity;
                }));
    }

    @Transactional(readOnly = true)
    public boolean isChannelEnabled(NotificationChannel channel) {
        return getEnabledChannel(channel).map(NotificationChannelEntity::getEnabled).orElse(false);
    }

    public Optional<Map<String, Object>> getUserPreferences(String userId) {
        return cachePort.getUserPreferences(userId);
    }

    public void saveUserPreferences(String userId, Map<String, Object> preferences) {
        cachePort.cacheUserPreferences(userId, preferences);
    }
}
