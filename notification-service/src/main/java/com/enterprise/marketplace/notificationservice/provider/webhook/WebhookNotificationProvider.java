package com.enterprise.marketplace.notificationservice.provider.webhook;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookNotificationProvider implements NotificationProvider {

    private final NotificationProperties notificationProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBHOOK;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Webhook URL is required in recipientAddress");
        }
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(notification.getRecipientAddress())
                    .build();
            Map<String, Object> payload = new HashMap<>();
            payload.put("notificationId", notification.getId().toString());
            payload.put("requestId", notification.getRequestId());
            payload.put("correlationId", notification.getCorrelationId());
            payload.put("recipientId", notification.getRecipientId());
            payload.put("subject", notification.getSubject());
            payload.put("body", notification.getBody());
            payload.put("notificationType", notification.getNotificationType().name());
            payload.put("channel", notification.getChannel().name());

            ResponseEntity<String> response = restClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            return ProviderDeliveryResult.success(
                    "Webhook responded with status " + response.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Webhook delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }
}
