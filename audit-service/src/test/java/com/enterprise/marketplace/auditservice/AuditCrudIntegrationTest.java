package com.enterprise.marketplace.auditservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.CreateAuditRequest;
import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false"
})
class AuditCrudIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndRetrieveAuditRecord() {
        String requestId = "int-audit-" + System.currentTimeMillis();
        String correlationId = UUID.randomUUID().toString();

        CreateAuditRequest request = CreateAuditRequest.builder()
                .requestId(requestId)
                .correlationId(correlationId)
                .sourceService("integration-test")
                .aggregateType("PRODUCT")
                .aggregateId(UUID.randomUUID())
                .operation(AuditOperation.CREATE)
                .actor("test-user")
                .afterState(java.util.Map.of("status", "CREATED"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<AuditResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/audits",
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getRequestId()).isEqualTo(requestId);

        UUID auditId = createResponse.getBody().getData().getId();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<AuditResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/audits/" + auditId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getCorrelationId()).isEqualTo(correlationId);
    }
}
