package com.enterprise.marketplace.notificationservice.provider.push;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnMissingBean(FcmPushProvider.class)
public class HttpPushProvider implements NotificationProvider {

    private final NotificationProperties notificationProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notificationProperties.getPushGatewayUrl())) {
            return ProviderDeliveryResult.failure("Push gateway URL is not configured");
        }
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Push device token is required in recipientAddress");
        }
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(notificationProperties.getPushGatewayUrl())
                    .build();
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceToken", notification.getRecipientAddress());
            payload.put("title", notification.getSubject());
            payload.put("body", notification.getBody());
            payload.put("notificationId", notification.getId().toString());
            payload.put("recipientId", notification.getRecipientId());
            ResponseEntity<String> response = restClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);
            return ProviderDeliveryResult.success(
                    "Push gateway responded with status " + response.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Push delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }
}
