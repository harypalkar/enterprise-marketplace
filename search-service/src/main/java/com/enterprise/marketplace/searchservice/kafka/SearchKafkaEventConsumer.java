package com.enterprise.marketplace.searchservice.kafka;

import com.enterprise.marketplace.searchservice.constants.SearchKafkaTopics;
import com.enterprise.marketplace.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SearchKafkaEventConsumer {

    private final SearchService searchService;

    @KafkaListener(
            topics = SearchKafkaTopics.SEARCH_INDEX,
            groupId = "${marketplace.kafka.consumer-group:search-service}")
    public void onSearchIndex(String payload) {
        handle(payload, "search-index");
    }

    @KafkaListener(
            topics = SearchKafkaTopics.PRODUCT_CREATED,
            groupId = "${marketplace.kafka.consumer-group:search-service}")
    public void onProductCreated(String payload) {
        handle(payload, "product-created");
    }

    @KafkaListener(
            topics = SearchKafkaTopics.PRODUCT_UPDATED,
            groupId = "${marketplace.kafka.consumer-group:search-service}")
    public void onProductUpdated(String payload) {
        handle(payload, "product-updated");
    }

    @KafkaListener(
            topics = SearchKafkaTopics.PRODUCT_DELETED,
            groupId = "${marketplace.kafka.consumer-group:search-service}")
    public void onProductDeleted(String payload) {
        handle(payload, "product-deleted");
    }

    private void handle(String payload, String eventSource) {
        try {
            searchService.processIndexEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
