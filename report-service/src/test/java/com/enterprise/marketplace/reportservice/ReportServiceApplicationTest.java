package com.enterprise.marketplace.reportservice;

import com.enterprise.marketplace.reportservice.audit.ReportAuditService;
import com.enterprise.marketplace.reportservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.reportservice.repository.ReportAuditRepository;
import com.enterprise.marketplace.reportservice.repository.ReportDefinitionRepository;
import com.enterprise.marketplace.reportservice.repository.ReportJobRepository;
import com.enterprise.marketplace.reportservice.repository.ReportResultRepository;
import com.enterprise.marketplace.reportservice.service.impl.ReportServiceImpl;
import com.enterprise.marketplace.reportservice.validation.ReportRequestValidator;
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
    "marketplace.report.generation-enabled=false"
})
class ReportServiceApplicationTest {

    @MockBean
    private ReportServiceImpl reportServiceImpl;

    @MockBean
    private ReportJobRepository reportJobRepository;

    @MockBean
    private ReportDefinitionRepository reportDefinitionRepository;

    @MockBean
    private ReportResultRepository reportResultRepository;

    @MockBean
    private ReportAuditRepository reportAuditRepository;

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private ReportRequestValidator reportRequestValidator;

    @MockBean
    private ReportAuditService reportAuditService;

    @Test
    void contextLoads() {}
}
