package com.enterprise.marketplace.notificationservice.provider.email;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import com.enterprise.marketplace.notificationservice.enums.TemplateContentType;
import com.enterprise.marketplace.notificationservice.provider.NotificationProvider;
import com.enterprise.marketplace.notificationservice.provider.ProviderDeliveryResult;
import com.enterprise.marketplace.notificationservice.repository.NotificationTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.notification", name = "email-provider", havingValue = "SMTP", matchIfMissing = true)
@ConditionalOnBean(JavaMailSender.class)
public class SmtpEmailProvider implements NotificationProvider {

    private final JavaMailSender mailSender;
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(notificationProperties.getDefaultFromEmail());
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(
                    StringUtils.hasText(notification.getSubject()) ? notification.getSubject() : "Notification");
            helper.setText(notification.getBody(), html);
            mailSender.send(message);
            return ProviderDeliveryResult.success("SMTP email sent to " + notification.getRecipientAddress());
        } catch (Exception ex) {
            log.error("SMTP email delivery failed notification id={}", notification.getId(), ex);
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
