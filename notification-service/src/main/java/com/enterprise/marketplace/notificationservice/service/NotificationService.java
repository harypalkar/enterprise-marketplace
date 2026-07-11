package com.enterprise.marketplace.notificationservice.service;

import com.enterprise.marketplace.notificationservice.dto.CreateNotificationRequest;
import com.enterprise.marketplace.notificationservice.dto.InboxPageResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationPageResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.notificationservice.dto.UpdateNotificationRequest;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotification(UUID notificationId);

    NotificationResponse getByRequestId(String requestId);

    NotificationPageResponse getByRecipientId(String recipientId, int page, int size);

    NotificationPageResponse getByStatus(NotificationStatus status, int page, int size);

    InboxPageResponse getInboxByRecipientId(String recipientId, int page, int size);

    NotificationResponse updateNotification(UUID notificationId, UpdateNotificationRequest request);

    NotificationResponse updateStatus(UUID notificationId, StatusUpdateRequest request);

    void deleteNotification(UUID notificationId);

    NotificationResponse retryNotification(UUID notificationId);

    void processFromKafkaEvent(String payload, String eventSource);

    void dispatchPendingNotifications();
}
