package com.enterprise.marketplace.subscriptionservice.kafka;

import com.enterprise.marketplace.subscriptionservice.constants.SubscriptionKafkaTopics;
import com.enterprise.marketplace.subscriptionservice.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionKafkaEventConsumer {

    private final SubscriptionService subscriptionService;

    @KafkaListener(
            topics = SubscriptionKafkaTopics.WORKFLOW_COMPLETED,
            groupId = "${marketplace.kafka.consumer-group:subscription-service}")
    public void onWorkflowCompleted(String payload) {
        try {
            subscriptionService.processWorkflowCompleted(payload);
        } catch (Exception ex) {
            log.error("Failed to process workflow-completed event", ex);
        }
    }
}
