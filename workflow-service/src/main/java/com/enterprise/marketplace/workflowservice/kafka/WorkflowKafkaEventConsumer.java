package com.enterprise.marketplace.workflowservice.kafka;

import com.enterprise.marketplace.workflowservice.constants.WorkflowKafkaTopics;
import com.enterprise.marketplace.workflowservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.service.WorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WorkflowKafkaEventConsumer {

    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = WorkflowKafkaTopics.PRODUCT_CREATED, groupId = "${marketplace.kafka.consumer-group:workflow-service}")
    public void onProductCreated(String payload) {
        handleExternalEvent(payload, WorkflowStatus.DATABASE_SAVED, "product-created");
    }

    @KafkaListener(topics = WorkflowKafkaTopics.PRODUCT_UPDATED, groupId = "${marketplace.kafka.consumer-group:workflow-service}")
    public void onProductUpdated(String payload) {
        handleExternalEvent(payload, WorkflowStatus.EVENT_PUBLISHED, "product-updated");
    }

    @KafkaListener(
            topics = WorkflowKafkaTopics.WORKFLOW_UPDATED,
            groupId = "${marketplace.kafka.consumer-group:workflow-service}")
    public void onWorkflowUpdated(String payload) {
        handleExternalEvent(payload, null, "workflow-updated");
    }

    private void handleExternalEvent(String payload, WorkflowStatus defaultStatus, String eventSource) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String requestId = extractText(root, "requestId");
            if (requestId == null) {
                requestId = extractText(root, "clientRequestId");
            }
            if (requestId == null) {
                log.warn("Skipping {} event without requestId", eventSource);
                return;
            }
            WorkflowStatus targetStatus = resolveTargetStatus(root, defaultStatus);
            if (targetStatus != null) {
                workflowService.advanceWorkflowFromEvent(requestId, targetStatus, eventSource, payload);
            }
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }

    private WorkflowStatus resolveTargetStatus(JsonNode root, WorkflowStatus defaultStatus) {
        String statusText = extractText(root, "status");
        if (statusText == null && root.has("workflow")) {
            statusText = extractText(root.get("workflow"), "status");
        }
        if (statusText != null) {
            try {
                return WorkflowStatus.valueOf(statusText);
            } catch (IllegalArgumentException ex) {
                log.warn("Unknown workflow status in event: {}", statusText);
            }
        }
        return defaultStatus;
    }

    private String extractText(JsonNode node, String field) {
        if (node != null && node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return null;
    }
}
