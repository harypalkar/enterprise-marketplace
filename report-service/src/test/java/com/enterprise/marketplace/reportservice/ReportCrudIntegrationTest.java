package com.enterprise.marketplace.reportservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.reportservice.dto.CreateReportJobRequest;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import java.util.Map;
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
    "marketplace.outbox.enabled=false",
    "marketplace.report.generation-enabled=false"
})
class ReportCrudIntegrationTest {

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
    void shouldCreateAndRetrieveReportJob() {
        String requestId = "int-report-" + System.currentTimeMillis();

        CreateReportJobRequest request = CreateReportJobRequest.builder()
                .requestId(requestId)
                .reportCode("SALES_SUMMARY")
                .requestedBy("test-user")
                .correlationId(UUID.randomUUID().toString())
                .parameters(Map.of("fromDate", "2026-07-01", "toDate", "2026-07-09"))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<ReportJobResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/reports/jobs",
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getRequestId()).isEqualTo(requestId);
        assertThat(createResponse.getBody().getData().getStatus()).isEqualTo(ReportJobStatus.PENDING);

        UUID jobId = createResponse.getBody().getData().getId();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<ReportJobResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/reports/jobs/" + jobId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getReportCode()).isEqualTo("SALES_SUMMARY");
    }

    @Test
    void shouldListReportDefinitions() {
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<java.util.List<com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse>>> response =
                restTemplate.exchange(
                        "/api/v1/reports/definitions",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(3);
    }
}
