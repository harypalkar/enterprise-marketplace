package com.enterprise.marketplace.notificationservice.provider.sms;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.notification", name = "twilio-enabled", havingValue = "true")
public class TwilioSmsProvider implements NotificationProvider {

    private final NotificationProperties notificationProperties;

    @PostConstruct
    void initTwilio() {
        Twilio.init(
                notificationProperties.getTwilioAccountSid(),
                notificationProperties.getTwilioAuthToken());
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Recipient phone number is required");
        }
        try {
            Message message = Message.creator(
                            new PhoneNumber(notification.getRecipientAddress()),
                            new PhoneNumber(notificationProperties.getTwilioFromNumber()),
                            notification.getBody())
                    .create();
            return ProviderDeliveryResult.success("Twilio sid=" + message.getSid());
        } catch (Exception ex) {
            log.error("Twilio SMS delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }
}
