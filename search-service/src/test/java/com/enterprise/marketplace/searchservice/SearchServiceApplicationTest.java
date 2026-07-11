package com.enterprise.marketplace.searchservice;

import com.enterprise.marketplace.searchservice.audit.SearchAuditService;
import com.enterprise.marketplace.searchservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.searchservice.repository.ProductSearchRepository;
import com.enterprise.marketplace.searchservice.repository.SearchAuditRepository;
import com.enterprise.marketplace.searchservice.repository.SearchSyncLogRepository;
import com.enterprise.marketplace.searchservice.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false",
    "management.health.elasticsearch.enabled=false",
    "management.endpoint.health.group.readiness.include=readinessState,ping"
})
class SearchServiceApplicationTest {

    @MockBean
    private SearchServiceImpl searchServiceImpl;

    @MockBean
    private ProductSearchRepository productSearchRepository;

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockBean
    private SearchAuditRepository searchAuditRepository;

    @MockBean
    private SearchSyncLogRepository searchSyncLogRepository;

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @MockBean
    private SearchAuditService searchAuditService;

    @Test
    void contextLoads() {}
}
