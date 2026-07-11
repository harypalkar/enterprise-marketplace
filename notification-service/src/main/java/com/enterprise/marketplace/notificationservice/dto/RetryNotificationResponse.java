package com.enterprise.marketplace.notificationservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryNotificationResponse {

    private List<NotificationResponse> retried;
    private int successCount;
    private int failureCount;
}
