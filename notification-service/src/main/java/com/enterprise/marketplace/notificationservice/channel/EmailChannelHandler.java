package com.enterprise.marketplace.notificationservice.channel;

import com.enterprise.marketplace.notificationservice.config.NotificationProperties;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
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
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
@ConditionalOnBean(JavaMailSender.class)
public class EmailChannelHandler implements NotificationChannelHandler {

    private final JavaMailSender mailSender;
    private final NotificationProperties notificationProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ChannelDeliveryResult deliver(NotificationEntity notification) {
        if (!StringUtils.hasText(notification.getRecipientAddress())) {
            return ChannelDeliveryResult.failure("Recipient email address is required");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(notificationProperties.getDefaultFromEmail());
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(
                    StringUtils.hasText(notification.getSubject())
                            ? notification.getSubject()
                            : "Notification");
            helper.setText(notification.getBody(), false);
            mailSender.send(message);
            return ChannelDeliveryResult.success("Email sent to " + notification.getRecipientAddress());
        } catch (Exception ex) {
            log.error("Failed to send email notification id={}", notification.getId(), ex);
            return ChannelDeliveryResult.failure(ex.getMessage());
        }
    }
}
