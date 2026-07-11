package com.enterprise.marketplace.workflowservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.workflowservice.dto.CreateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
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
class WorkflowCrudIntegrationTest {

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
    void shouldCreateTransitionAndRetrieveWorkflow() {
        String requestId = "int-req-" + System.currentTimeMillis();
        UUID aggregateId = UUID.randomUUID();

        CreateWorkflowRequest createRequest = CreateWorkflowRequest.builder()
                .requestId(requestId)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(AggregateType.PRODUCT)
                .aggregateId(aggregateId)
                .operationType(WorkflowOperationType.CREATE)
                .sourceSystem("integration-test")
                .initiatedBy("test-user")
                .initialStatus(WorkflowStatus.INITIAL)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<WorkflowResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/workflows",
                        HttpMethod.POST,
                        new HttpEntity<>(createRequest, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getRequestId()).isEqualTo(requestId);
        assertThat(createResponse.getBody().getData().getStatus()).isEqualTo(WorkflowStatus.INITIAL);

        UUID workflowId = createResponse.getBody().getData().getId();

        StatusUpdateRequest statusRequest = StatusUpdateRequest.builder()
                .targetStatus(WorkflowStatus.RECEIVED)
                .reason("Integration test transition")
                .build();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<WorkflowResponse>> patchResponse =
                restTemplate.exchange(
                        "/api/v1/workflows/" + workflowId + "/status",
                        HttpMethod.PATCH,
                        new HttpEntity<>(statusRequest, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResponse.getBody().getData().getStatus()).isEqualTo(WorkflowStatus.RECEIVED);

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<WorkflowResponse>> getByRequest =
                restTemplate.exchange(
                        "/api/v1/workflows/request/" + requestId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getByRequest.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByRequest.getBody().getData().getStatus()).isEqualTo(WorkflowStatus.RECEIVED);
    }
}
