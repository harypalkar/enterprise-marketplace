package com.enterprise.marketplace.notificationservice;

import com.enterprise.marketplace.notificationservice.audit.NotificationAuditService;
import com.enterprise.marketplace.notificationservice.repository.NotificationInboxRepository;
import com.enterprise.marketplace.notificationservice.repository.NotificationRepository;
import com.enterprise.marketplace.notificationservice.service.impl.NotificationServiceImpl;
import com.enterprise.marketplace.notificationservice.validation.NotificationRequestValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false",
    "spring.mail.host="
})
class NotificationServiceApplicationTest {

    @MockBean
    private NotificationServiceImpl notificationServiceImpl;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private NotificationAuditService notificationAuditService;

    @MockBean
    private NotificationRequestValidator notificationRequestValidator;

    @MockBean
    private NotificationInboxRepository notificationInboxRepository;

    @Test
    void contextLoads() {}
}
