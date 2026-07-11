package com.enterprise.marketplace.notificationservice.dto;

import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class SendNotificationRequest {

    @NotBlank
    private String requestId;

    private String correlationId;

    private UUID workflowId;

    private String aggregateType;

    private UUID aggregateId;

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private NotificationChannel channel;

    @NotBlank
    private String recipientId;

    private String recipientAddress;

    private String subject;

    private String body;

    private String templateCode;

    private Map<String, String> templateVariables;

    private Map<String, Object> metadata;

    private List<NotificationChannel> fallbackChannels;
}
