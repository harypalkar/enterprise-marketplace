package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;

public interface NotificationChannelHandler {

    NotificationChannel getChannel();

    ChannelDeliveryResult deliver(NotificationEntity notification);
}
