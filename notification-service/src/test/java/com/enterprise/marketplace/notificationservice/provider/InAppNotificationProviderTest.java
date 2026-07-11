package com.enterprise.marketplace.notificationservice.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.enums.NotificationType;
import com.enterprise.marketplace.notificationservice.provider.inapp.InAppNotificationProvider;
import com.enterprise.marketplace.notificationservice.repository.NotificationInboxRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InAppNotificationProviderTest {

    @Mock
    private NotificationInboxRepository inboxRepository;

    @InjectMocks
    private InAppNotificationProvider provider;

    @Test
    void shouldDeliverInAppNotification() {
        NotificationEntity notification = buildNotification();
        ProviderDeliveryResult result = provider.deliver(notification);
        assertThat(result.success()).isTrue();
        assertThat(result.response()).contains("In-app notification saved");
    }

    private NotificationEntity buildNotification() {
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID());
        notification.setRequestId("req-1");
        notification.setNotificationType(NotificationType.CUSTOM);
        notification.setChannel(NotificationChannel.IN_APP);
        notification.setRecipientId("user-1");
        notification.setBody("Test body");
        notification.setStatus(NotificationStatus.CREATED);
        return notification;
    }
}
