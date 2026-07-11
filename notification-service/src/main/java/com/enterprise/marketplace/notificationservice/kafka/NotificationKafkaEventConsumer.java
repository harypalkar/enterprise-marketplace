package com.enterprise.marketplace.notificationservice.kafka;

import com.enterprise.marketplace.notificationservice.constants.NotificationKafkaTopics;
import com.enterprise.marketplace.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationKafkaEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = NotificationKafkaTopics.NOTIFICATION_CREATED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onNotificationCreated(String payload) {
        handleEvent(payload, "notification-created");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.WORKFLOW_COMPLETED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onWorkflowCompleted(String payload) {
        handleEvent(payload, "workflow-completed");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.WORKFLOW_FAILED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onWorkflowFailed(String payload) {
        handleEvent(payload, "workflow-failed");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.PRODUCT_CREATED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onProductCreated(String payload) {
        handleEvent(payload, "product-created");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.PRODUCT_UPDATED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onProductUpdated(String payload) {
        handleEvent(payload, "product-updated");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.SELLER_APPROVED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onSellerApproved(String payload) {
        handleEvent(payload, "seller-approved");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.BUYER_REGISTERED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onBuyerRegistered(String payload) {
        handleEvent(payload, "buyer-registered");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.INVENTORY_LOW,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onInventoryLow(String payload) {
        handleEvent(payload, "inventory-low");
    }

    @KafkaListener(
            topics = NotificationKafkaTopics.SUBSCRIPTION_EXPIRED,
            groupId = "${marketplace.kafka.consumer-group:notification-service}")
    public void onSubscriptionExpired(String payload) {
        handleEvent(payload, "subscription-expired");
    }

    private void handleEvent(String payload, String eventSource) {
        try {
            notificationService.processFromKafkaEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
