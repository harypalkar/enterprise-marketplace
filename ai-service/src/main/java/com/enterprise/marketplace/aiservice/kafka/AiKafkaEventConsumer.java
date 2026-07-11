package com.enterprise.marketplace.aiservice.kafka;

import com.enterprise.marketplace.aiservice.constants.AiKafkaTopics;
import com.enterprise.marketplace.aiservice.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiKafkaEventConsumer {

    private final AiService aiService;

    @KafkaListener(
            topics = AiKafkaTopics.PRODUCT_CREATED,
            groupId = "${marketplace.kafka.consumer-group:ai-service}")
    public void onProductCreated(String payload) {
        handle(payload, "product-created");
    }

    @KafkaListener(
            topics = AiKafkaTopics.PRODUCT_UPDATED,
            groupId = "${marketplace.kafka.consumer-group:ai-service}")
    public void onProductUpdated(String payload) {
        handle(payload, "product-updated");
    }

    @KafkaListener(
            topics = AiKafkaTopics.SEARCH_INDEX,
            groupId = "${marketplace.kafka.consumer-group:ai-service}")
    public void onSearchIndex(String payload) {
        handle(payload, "search-index");
    }

    @KafkaListener(
            topics = AiKafkaTopics.ADMIN_FEATURE_TOGGLED,
            groupId = "${marketplace.kafka.consumer-group:ai-service}")
    public void onFeatureToggled(String payload) {
        handle(payload, "admin-feature-toggled");
    }

    private void handle(String payload, String eventSource) {
        try {
            aiService.processKafkaEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
