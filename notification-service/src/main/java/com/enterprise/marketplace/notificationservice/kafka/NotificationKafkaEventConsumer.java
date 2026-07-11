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

    private void handleEvent(String payload, String eventSource) {
        try {
            notificationService.processFromKafkaEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
