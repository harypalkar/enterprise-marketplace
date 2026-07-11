package com.enterprise.marketplace.notificationservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.notificationservice.dto.CreateNotificationRequest;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.repository.NotificationRepository;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class NotificationRequestValidator {

    private static final Map<NotificationStatus, Set<NotificationStatus>> ALLOWED_TRANSITIONS = Map.of(
            NotificationStatus.PENDING,
                    EnumSet.of(
                            NotificationStatus.QUEUED,
                            NotificationStatus.PROCESSING,
                            NotificationStatus.CANCELLED),
            NotificationStatus.QUEUED,
                    EnumSet.of(NotificationStatus.PROCESSING, NotificationStatus.CANCELLED),
            NotificationStatus.PROCESSING,
                    EnumSet.of(
                            NotificationStatus.SENT,
                            NotificationStatus.DELIVERED,
                            NotificationStatus.FAILED,
                            NotificationStatus.RETRY),
            NotificationStatus.RETRY,
                    EnumSet.of(
                            NotificationStatus.QUEUED,
                            NotificationStatus.PROCESSING,
                            NotificationStatus.CANCELLED),
            NotificationStatus.SENT,
                    EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.FAILED),
            NotificationStatus.FAILED,
                    EnumSet.of(NotificationStatus.RETRY, NotificationStatus.CANCELLED),
            NotificationStatus.DELIVERED, EnumSet.noneOf(NotificationStatus.class),
            NotificationStatus.CANCELLED, EnumSet.noneOf(NotificationStatus.class));

    private final NotificationRepository notificationRepository;

    public void validateCreateRequest(CreateNotificationRequest request) {
        validateRequestIdUnique(request.getRequestId());
        validateChannelRequirements(request.getChannel(), request.getRecipientAddress(), request.getBody(), request.getTemplateCode());
    }

    public void validateTransition(NotificationStatus fromStatus, NotificationStatus toStatus) {
        if (fromStatus == toStatus) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Notification is already in status " + toStatus);
        }
        Set<NotificationStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(fromStatus, Set.of());
        if (!allowed.contains(toStatus)) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Transition from " + fromStatus + " to " + toStatus + " is not allowed");
        }
    }

    public void validateRequestIdUnique(String requestId) {
        notificationRepository.findByRequestIdAndActiveTrue(requestId).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Notification already exists for requestId " + requestId);
        });
    }

    public void validateRetryAllowed(NotificationStatus status, int retryCount, int maxRetries) {
        if (status != NotificationStatus.FAILED && status != NotificationStatus.RETRY) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Retry is only allowed for FAILED or RETRY notifications");
        }
        if (retryCount >= maxRetries) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Maximum retry count reached for notification");
        }
    }

    public void validateChannelRequirements(
            NotificationChannel channel, String recipientAddress, String body, String templateCode) {
        if (channel != NotificationChannel.IN_APP && !StringUtils.hasText(recipientAddress)) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR,
                    "recipientAddress is required for channel " + channel);
        }
        if (!StringUtils.hasText(body) && !StringUtils.hasText(templateCode)) {
            throw new MarketplaceException(
                    ErrorCode.VALIDATION_ERROR, "Either body or templateCode must be provided");
        }
    }
}
