package com.enterprise.marketplace.notificationservice.provider.email;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.TemplateContentType;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.enterprise.marketplace.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.notification", name = "email-provider", havingValue = "SES")
public class SesEmailProvider implements NotificationProvider {

    private final SesClient sesClient;
    private final NotificationProperties notificationProperties;
    private final NotificationTemplateRepository templateRepository;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ProviderDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ProviderDeliveryResult.failure("Recipient email address is required");
        }
        try {
            boolean html = resolveHtml(notification);
            Content subject = Content.builder()
                    .data(StringUtils.hasText(notification.getSubject()) ? notification.getSubject() : "Notification")
                    .charset("UTF-8")
                    .build();
            Content bodyContent =
                    Content.builder().data(notification.getBody()).charset("UTF-8").build();
            Body body = html
                    ? Body.builder().html(bodyContent).build()
                    : Body.builder().text(bodyContent).build();
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(notificationProperties.getDefaultFromEmail())
                    .destination(Destination.builder()
                            .toAddresses(notification.getRecipientAddress())
                            .build())
                    .message(Message.builder().subject(subject).body(body).build())
                    .build();
            var response = sesClient.sendEmail(request);
            return ProviderDeliveryResult.success("SES messageId=" + response.messageId());
        } catch (Exception ex) {
            log.error("SES email delivery failed notification id={}", notification.getId(), ex);
            return ProviderDeliveryResult.failure(ex.getMessage());
        }
    }

    private boolean resolveHtml(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getTemplateCode())) {
            return notification.getBody() != null && notification.getBody().contains("<html");
        }
        return templateRepository
                .findByTemplateCodeAndChannelAndActiveTrue(
                        notification.getTemplateCode(), NotificationChannel.EMAIL)
                .map(template -> template.getContentType() == TemplateContentType.HTML)
                .orElse(false);
    }
}
