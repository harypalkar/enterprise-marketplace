package com.enterprise.marketplace.auditservice;

import com.enterprise.marketplace.auditservice.repository.AuditRecordRepository;
import com.enterprise.marketplace.auditservice.service.impl.AuditServiceImpl;
import com.enterprise.marketplace.auditservice.validation.AuditRequestValidator;
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
class AuditServiceApplicationTest {

    @MockBean
    private AuditServiceImpl auditServiceImpl;

    @MockBean
    private AuditRecordRepository auditRecordRepository;

    @MockBean
    private AuditRequestValidator auditRequestValidator;

    @Test
    void contextLoads() {}
}
