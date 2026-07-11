package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationInboxEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.repository.NotificationInboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppChannelHandler implements NotificationChannelHandler {

    private final NotificationInboxRepository inboxRepository;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public ChannelDeliveryResult deliver(NotificationEntity notification) {
        NotificationInboxEntity inbox = new NotificationInboxEntity();
        inbox.setNotificationId(notification.getId());
        inbox.setRecipientId(notification.getRecipientId());
        inbox.setSubject(notification.getSubject());
        inbox.setBody(notification.getBody());
        inbox.setReadFlag(Boolean.FALSE);
        inboxRepository.save(inbox);
        return ChannelDeliveryResult.success("In-app notification saved to inbox id=" + inbox.getId());
    }
}
