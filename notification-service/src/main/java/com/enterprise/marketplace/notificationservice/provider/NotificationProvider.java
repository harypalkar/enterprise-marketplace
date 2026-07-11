package com.enterprise.marketplace.notificationservice.provider;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;

public interface NotificationProvider {

    NotificationChannel getChannel();

    ProviderDeliveryResult deliver(NotificationEntity notification);
}
