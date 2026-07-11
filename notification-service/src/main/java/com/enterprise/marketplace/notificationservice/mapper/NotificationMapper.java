package com.enterprise.marketplace.notificationservice.mapper;

import com.enterprise.marketplace.notificationservice.dto.InboxResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationInboxEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ObjectMapper objectMapper;

    public NotificationResponse toResponse(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .workflowId(entity.getWorkflowId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .notificationType(entity.getNotificationType())
                .channel(entity.getChannel())
                .recipientId(entity.getRecipientId())
                .recipientAddress(entity.getRecipientAddress())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .status(entity.getStatus())
                .previousStatus(entity.getPreviousStatus())
                .templateCode(entity.getTemplateCode())
                .metadata(deserializeMetadata(entity.getMetadata()))
                .retryCount(entity.getRetryCount())
                .maxRetries(entity.getMaxRetries())
                .active(entity.getActive())
                .sentAt(entity.getSentAt())
                .deliveredAt(entity.getDeliveredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public InboxResponse toInboxResponse(NotificationInboxEntity entity) {
        return InboxResponse.builder()
                .id(entity.getId())
                .notificationId(entity.getNotificationId())
                .recipientId(entity.getRecipientId())
                .subject(entity.getSubject())
                .body(entity.getBody())
                .readFlag(entity.getReadFlag())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
