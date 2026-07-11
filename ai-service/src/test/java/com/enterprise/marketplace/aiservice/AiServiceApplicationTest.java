package com.enterprise.marketplace.aiservice;

import com.enterprise.marketplace.aiservice.repository.AiAuditRepository;
import com.enterprise.marketplace.aiservice.repository.AiChatMessageRepository;
import com.enterprise.marketplace.aiservice.repository.AiChatSessionRepository;
import com.enterprise.marketplace.aiservice.repository.AiGenerationLogRepository;
import com.enterprise.marketplace.aiservice.repository.AiPromptTemplateRepository;
import com.enterprise.marketplace.aiservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.aiservice.service.impl.AiServiceImpl;
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
    "marketplace.outbox.enabled=false",
    "marketplace.ollama.enabled=false",
    "management.endpoint.health.group.readiness.include=readinessState,ping"
})
class AiServiceApplicationTest {

    @MockBean
    private AiServiceImpl aiServiceImpl;

    @MockBean
    private AiPromptTemplateRepository promptTemplateRepository;

    @MockBean
    private AiChatSessionRepository chatSessionRepository;

    @MockBean
    private AiChatMessageRepository chatMessageRepository;

    @MockBean
    private AiGenerationLogRepository generationLogRepository;

    @MockBean
    private AiAuditRepository auditRepository;

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @Test
    void contextLoads() {}
}
