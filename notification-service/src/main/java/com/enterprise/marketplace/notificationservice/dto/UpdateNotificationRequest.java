package com.enterprise.marketplace.notificationservice.dto;

import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.Size;
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
public class UpdateNotificationRequest {

    private UUID workflowId;

    @Size(max = 64)
    private String aggregateType;

    private UUID aggregateId;

    private NotificationChannel channel;

    @Size(max = 128)
    private String recipientId;

    @Size(max = 512)
    private String recipientAddress;

    @Size(max = 500)
    private String subject;

    @Size(max = 10000)
    private String body;

    @Size(max = 64)
    private String templateCode;

    private Map<String, Object> metadata;

    private Map<String, String> templateVariables;
}
