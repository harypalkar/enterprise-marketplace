package com.enterprise.marketplace.notificationservice.dto;

import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.enums.NotificationType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String requestId;
    private String correlationId;
    private UUID workflowId;
    private String aggregateType;
    private UUID aggregateId;
    private NotificationType notificationType;
    private NotificationChannel channel;
    private String recipientId;
    private String recipientAddress;
    private String subject;
    private String body;
    private NotificationStatus status;
    private NotificationStatus previousStatus;
    private String templateCode;
    private Map<String, Object> metadata;
    private Integer retryCount;
    private Integer maxRetries;
    private Boolean active;
    private Instant sentAt;
    private Instant deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;
}
