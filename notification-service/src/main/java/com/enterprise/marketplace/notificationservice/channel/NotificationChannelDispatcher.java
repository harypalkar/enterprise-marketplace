package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.enterprise.marketplace.notificationservice.provider.email.SesEmailProvider;
import com.enterprise.marketplace.notificationservice.provider.email.SmtpEmailProvider;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NotificationChannelDispatcher {

    private final Map<NotificationChannel, NotificationProvider> providers;

    public NotificationChannelDispatcher(List<NotificationProvider> providerList) {
        this.providers = new EnumMap<>(NotificationChannel.class);
        for (NotificationProvider provider : providerList) {
            if (provider instanceof SmtpEmailProvider || provider instanceof SesEmailProvider) {
                continue;
            }
            providers.put(provider.getChannel(), provider);
        }
    }

    public ChannelDeliveryResult dispatch(NotificationEntity notification) {
        NotificationProvider provider = providers.get(notification.getChannel());
        if (provider == null) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "No provider registered for channel " + notification.getChannel());
        }
        ProviderDeliveryResult result = provider.deliver(notification);
        return result.success()
                ? ChannelDeliveryResult.success(result.response())
                : ChannelDeliveryResult.failure(result.errorMessage());
    }
}
