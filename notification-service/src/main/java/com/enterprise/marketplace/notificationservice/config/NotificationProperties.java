package com.enterprise.marketplace.notificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.notification")
public class NotificationProperties {

    private long dispatchIntervalMs = 3000L;
    private int maxDeliveryRetries = 3;
    private String defaultFromEmail = "noreply@marketplace.local";
    private String smsGatewayUrl = "";
    private String pushGatewayUrl = "";
    private long webhookTimeoutMs = 5000L;
}
