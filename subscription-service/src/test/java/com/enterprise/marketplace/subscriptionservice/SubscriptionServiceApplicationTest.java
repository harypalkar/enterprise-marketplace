package com.enterprise.marketplace.subscriptionservice;

import com.enterprise.marketplace.subscriptionservice.audit.SubscriptionAuditService;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionBillingRepository;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionPlanRepository;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionRepository;
import com.enterprise.marketplace.subscriptionservice.service.impl.SubscriptionPlanServiceImpl;
import com.enterprise.marketplace.subscriptionservice.service.impl.SubscriptionServiceImpl;
import com.enterprise.marketplace.subscriptionservice.validation.SubscriptionRequestValidator;
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
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false"
})
class SubscriptionServiceApplicationTest {

    @MockBean
    private SubscriptionServiceImpl subscriptionServiceImpl;

    @MockBean
    private SubscriptionPlanServiceImpl subscriptionPlanServiceImpl;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @MockBean
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @MockBean
    private SubscriptionBillingRepository subscriptionBillingRepository;

    @MockBean
    private SubscriptionAuditService subscriptionAuditService;

    @MockBean
    private SubscriptionRequestValidator subscriptionRequestValidator;

    @Test
    void contextLoads() {}
}
