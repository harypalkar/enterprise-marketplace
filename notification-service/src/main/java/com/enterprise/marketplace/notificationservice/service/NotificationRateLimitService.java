package com.enterprise.marketplace.notificationservice.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.redis.NotificationCachePort;
import com.enterprise.marketplace.notificationservice.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRateLimitService {

    private final NotificationCachePort cachePort;
    private final NotificationChannelRepository channelRepository;

    public void validateRateLimit(String recipientId, NotificationChannel channel) {
        NotificationChannelEntity config = channelRepository
                .findByChannelAndEnabledTrue(channel)
                .orElseThrow(() -> new MarketplaceException(
                        ErrorCode.BUSINESS_RULE_VIOLATION, "Channel is disabled: " + channel));
        int limit = config.getRateLimitPerHour() != null ? config.getRateLimitPerHour() : 100;
        if (cachePort.isRateLimitExceeded(recipientId, channel.name(), limit)) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Rate limit exceeded for recipient " + recipientId + " on channel " + channel);
        }
        cachePort.incrementRateLimit(recipientId, channel.name());
    }
}
