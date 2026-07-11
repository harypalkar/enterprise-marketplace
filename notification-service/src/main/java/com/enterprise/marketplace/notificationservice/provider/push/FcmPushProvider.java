package com.enterprise.marketplace.notificationservice.provider.push;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.notification", name = "fcm-enabled", havingValue = "true")
public class FcmPushProvider implements NotificationProvider {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Push device token is required in recipientAddress");
        }
        try {
            Message message = Message.builder()
                    .setToken(notification.getRecipientAddress())
                    .setNotification(Notification.builder()
                            .setTitle(StringUtils.hasText(notification.getSubject()) ? notification.getSubject() : "Notification")
                            .setBody(notification.getBody())
                            .build())
                    .putData("notificationId", notification.getId().toString())
                    .putData("recipientId", notification.getRecipientId())
                    .build();
            String messageId = firebaseMessaging.send(message);
            return ProviderDeliveryResult.success("FCM messageId=" + messageId);
        } catch (Exception ex) {
            log.error("FCM push delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }
}
