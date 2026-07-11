package com.enterprise.marketplace.auditservice.kafka;

import com.enterprise.marketplace.auditservice.constants.AuditKafkaTopics;
import com.enterprise.marketplace.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditKafkaEventConsumer {

    private final AuditService auditService;

    @KafkaListener(
            topics = AuditKafkaTopics.AUDIT_CREATED,
            groupId = "${marketplace.kafka.consumer-group:audit-service}")
    public void onAuditCreated(String payload) {
        handleEvent(payload, "audit-created");
    }

    private void handleEvent(String payload, String eventSource) {
        try {
            auditService.processFromKafkaEvent(payload, eventSource);
        } catch (Exception ex) {
            log.error("Failed to process {} event", eventSource, ex);
        }
    }
}
