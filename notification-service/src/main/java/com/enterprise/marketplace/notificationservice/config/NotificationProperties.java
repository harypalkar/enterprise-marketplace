package com.enterprise.marketplace.notificationservice.config;

import com.enterprise.marketplace.notificationservice.enums.EmailProviderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.notification")
public class NotificationProperties {

    private long dispatchIntervalMs = 3000L;
    private int maxDeliveryRetries = 3;
    private long expiryHours = 72L;
    private String defaultFromEmail = "noreply@marketplace.local";
    private EmailProviderType emailProvider = EmailProviderType.SMTP;
    private String smsGatewayUrl = "";
    private String pushGatewayUrl = "";
    private long webhookTimeoutMs = 5000L;
    private String awsRegion = "ap-south-1";
    private boolean twilioEnabled = false;
    private String twilioAccountSid = "";
    private String twilioAuthToken = "";
    private String twilioFromNumber = "";
    private boolean fcmEnabled = false;
    private String fcmServiceAccountPath = "";
}
