package com.enterprise.marketplace.identityservice.infrastructure.client;

import com.enterprise.marketplace.identityservice.config.AuthProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestClient.Builder restClientBuilder;
    private final AuthProperties authProperties;

    @Value("${marketplace.notification.base-url:http://localhost:8089}")
    private String notificationBaseUrl;

    public void sendOtpSms(String countryCode, String mobileNumber, String otp) {
        if (!authProperties.getOtp().isSmsEnabled()) {
            log.info("SMS disabled; skipping OTP SMS to {}{}", countryCode, mobileNumber);
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("requestId", UUID.randomUUID().toString());
            body.put("notificationType", "CUSTOM");
            body.put("channel", "SMS");
            body.put("recipientId", countryCode + mobileNumber);
            body.put("recipientAddress", countryCode + mobileNumber);
            body.put("subject", "KaratKart OTP");
            body.put("body", "Your KaratKart verification code is " + otp + ". Valid for 5 minutes.");

            restClientBuilder
                    .build()
                    .post()
                    .uri(notificationBaseUrl + "/api/v1/notifications/send")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send OTP SMS via notification-service: {}", ex.getMessage());
        }
    }
}
