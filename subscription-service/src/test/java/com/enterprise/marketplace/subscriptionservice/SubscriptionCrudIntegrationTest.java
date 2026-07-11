package com.enterprise.marketplace.subscriptionservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.subscriptionservice.dto.PlanListResponse;
import com.enterprise.marketplace.subscriptionservice.dto.SubscribeRequest;
import com.enterprise.marketplace.subscriptionservice.dto.SubscriptionResponse;
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
class SubscriptionCrudIntegrationTest {

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
    void shouldListSeededPlansAndCreateSubscription() {
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<PlanListResponse>> plansResponse =
                restTemplate.exchange(
                        "/api/v1/plans",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(plansResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plansResponse.getBody()).isNotNull();
        assertThat(plansResponse.getBody().getData().getTotalElements()).isGreaterThanOrEqualTo(3);

        String requestId = "int-sub-" + System.currentTimeMillis();
        SubscribeRequest request = SubscribeRequest.builder()
                .requestId(requestId)
                .correlationId(UUID.randomUUID().toString())
                .sellerId(UUID.randomUUID())
                .buyerId(UUID.randomUUID())
                .planCode("BASIC")
                .autoRenew(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<SubscriptionResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/subscriptions",
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getRequestId()).isEqualTo(requestId);
        assertThat(createResponse.getBody().getData().getPlanCode()).isEqualTo("BASIC");

        UUID subscriptionId = createResponse.getBody().getData().getId();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<SubscriptionResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/subscriptions/" + subscriptionId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getAutoRenew()).isTrue();
    }
}
