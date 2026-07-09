package com.enterprise.marketplace.pricingservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.pricingservice.application.dto.CreatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.PricingResponse;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false"
})
class PricingCrudIntegrationTest {

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
    void shouldCreateAndRetrievePricing() {
        CreatePricingRequest request = CreatePricingRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .unitPrice(new BigDecimal("2499.50"))
                .currency("INR")
                .minQuantity(10)
                .discountPercent(new BigDecimal("12.50"))
                .validFrom(Instant.parse("2026-07-01T00:00:00Z"))
                .validTo(Instant.parse("2026-12-31T23:59:59Z"))
                .status(PricingStatus.ACTIVE)
                .build();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<PricingResponse>> createResponse = restTemplate.exchange(
                "/api/v1/pricing",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getCurrency()).isEqualTo("INR");

        UUID pricingId = createResponse.getBody().getData().getId();
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<PricingResponse>> getResponse = restTemplate.exchange(
                "/api/v1/pricing/" + pricingId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getUnitPrice()).isEqualByComparingTo("2499.50");
    }
}
