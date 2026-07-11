package com.enterprise.marketplace.notificationservice.provider.sms;

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
@ConditionalOnMissingBean(TwilioSmsProvider.class)
public class HttpSmsProvider implements NotificationProvider {

    private final NotificationProperties notificationProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notificationProperties.getSmsGatewayUrl())) {
            return ProviderDeliveryResult.failure("SMS gateway URL is not configured");
        }
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Recipient phone number is required");
        }
        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(notificationProperties.getSmsGatewayUrl())
                    .build();
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", notification.getRecipientAddress());
            payload.put("message", notification.getBody());
            payload.put("notificationId", notification.getId().toString());
            ResponseEntity<String> response = restClient
                    .post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);
            return ProviderDeliveryResult.success(
                    "SMS gateway responded with status " + response.getStatusCode().value());
        } catch (Exception ex) {
            log.error("SMS delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }
}
