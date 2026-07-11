package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationChannelDispatcher {

    private final Map<NotificationChannel, NotificationChannelHandler> handlers;

    public NotificationChannelDispatcher(List<NotificationChannelHandler> handlerList) {
        this.handlers = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelHandler handler : handlerList) {
            handlers.put(handler.getChannel(), handler);
        }
    }

    public ChannelDeliveryResult dispatch(NotificationEntity notification) {
        NotificationChannelHandler handler = handlers.get(notification.getChannel());
        if (handler == null) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "No handler registered for channel " + notification.getChannel());
        }
        return handler.deliver(notification);
    }
}
