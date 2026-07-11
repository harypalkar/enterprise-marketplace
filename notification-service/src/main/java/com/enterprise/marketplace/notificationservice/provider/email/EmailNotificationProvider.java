package com.enterprise.marketplace.notificationservice.provider.email;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.EmailProviderType;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationProvider implements NotificationProvider {

    private final NotificationProperties notificationProperties;
    private final List<NotificationProvider> emailProviders;

    public EmailNotificationProvider(
            NotificationProperties notificationProperties, List<NotificationProvider> allProviders) {
        this.notificationProperties = notificationProperties;
        this.emailProviders = allProviders.stream()
                .filter(provider -> provider.getChannel() == NotificationChannel.EMAIL
                        && provider.getClass() != EmailNotificationProvider.class)
                .toList();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        EmailProviderType configured = notificationProperties.getEmailProvider();
        for (NotificationProvider provider : emailProviders) {
            if (matchesProvider(provider, configured)) {
                return provider.deliver(notification);
            }
        }
        return ProviderDeliveryResult.failure("No email provider configured for " + configured);
    }

    private boolean matchesProvider(NotificationProvider provider, EmailProviderType configured) {
        if (configured == EmailProviderType.SES) {
            return provider instanceof SesEmailProvider;
        }
        return provider instanceof SmtpEmailProvider;
    }
}
