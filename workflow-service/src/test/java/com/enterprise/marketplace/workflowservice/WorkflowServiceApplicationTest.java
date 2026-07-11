package com.enterprise.marketplace.workflowservice;

import com.enterprise.marketplace.workflowservice.audit.WorkflowAuditService;
import com.enterprise.marketplace.workflowservice.repository.WorkflowRepository;
import com.enterprise.marketplace.workflowservice.service.impl.WorkflowServiceImpl;
import com.enterprise.marketplace.workflowservice.validation.WorkflowStatusTransitionValidator;
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
class WorkflowServiceApplicationTest {

    @MockBean
    private WorkflowServiceImpl workflowServiceImpl;

    @MockBean
    private WorkflowRepository workflowRepository;

    @MockBean
    private WorkflowAuditService workflowAuditService;

    @MockBean
    private WorkflowStatusTransitionValidator transitionValidator;

    @Test
    void contextLoads() {}
}
