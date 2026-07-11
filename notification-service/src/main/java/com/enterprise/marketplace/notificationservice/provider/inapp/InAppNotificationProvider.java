package com.enterprise.marketplace.notificationservice.provider.inapp;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationInboxEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.enterprise.marketplace.notificationservice.repository.NotificationInboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppNotificationProvider implements NotificationProvider {

    private final NotificationInboxRepository inboxRepository;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        NotificationInboxEntity inbox = new NotificationInboxEntity();
        inbox.setNotificationId(notification.getId());
        inbox.setRecipientId(notification.getRecipientId());
        inbox.setSubject(notification.getSubject());
        inbox.setBody(notification.getBody());
        inbox.setReadFlag(Boolean.FALSE);
        inboxRepository.save(inbox);
        return ProviderDeliveryResult.success("In-app notification saved to inbox id=" + inbox.getId());
    }
}
