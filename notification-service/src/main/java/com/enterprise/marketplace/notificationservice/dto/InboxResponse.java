package com.enterprise.marketplace.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboxResponse {

    private UUID id;
    private UUID notificationId;
    private String recipientId;
    private String subject;
    private String body;
    private Boolean readFlag;
    private Instant readAt;
    private Instant createdAt;
}
