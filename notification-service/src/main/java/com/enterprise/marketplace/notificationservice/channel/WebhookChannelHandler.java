package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebhookChannelHandler implements NotificationChannelHandler {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WEBHOOK;
    }

    @Override
    public ChannelDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ChannelDeliveryResult.failure("Webhook URL is required in recipientAddress");
        }
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(notification.getRecipientAddress())
                    .build();
            Map<String, Object> payload = new HashMap<>();
            payload.put("notificationId", notification.getId().toString());
            payload.put("requestId", notification.getRequestId());
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

            return ChannelDeliveryResult.success(
                    "Webhook responded with status " + response.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Webhook delivery failed notification id={}", notification.getId(), ex);
            return ChannelDeliveryResult.failure(ex.getMessage());
        }
    }
}
