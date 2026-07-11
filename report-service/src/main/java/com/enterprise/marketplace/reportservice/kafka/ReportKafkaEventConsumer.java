package com.enterprise.marketplace.reportservice.kafka;

import com.enterprise.marketplace.reportservice.constants.ReportKafkaTopics;
import com.enterprise.marketplace.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReportKafkaEventConsumer {

    private final ReportService reportService;

    @KafkaListener(
            topics = ReportKafkaTopics.WORKFLOW_COMPLETED,
            groupId = "${marketplace.kafka.consumer-group:report-service}")
    public void onWorkflowCompleted(String payload) {
        handleEvent(payload, "workflow-completed");
    }

    @KafkaListener(
            topics = ReportKafkaTopics.PRODUCT_CREATED,
            groupId = "${marketplace.kafka.consumer-group:report-service}")
    public void onProductCreated(String payload) {
        handleEvent(payload, "product-created");
    }

    private void handleEvent(String payload, String eventSource) {
        try {
            reportService.processExternalEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
