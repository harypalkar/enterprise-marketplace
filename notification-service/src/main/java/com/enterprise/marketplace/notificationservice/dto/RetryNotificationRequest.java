package com.enterprise.marketplace.notificationservice.dto;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryNotificationRequest {

    private UUID notificationId;

    private List<UUID> notificationIds;
}
