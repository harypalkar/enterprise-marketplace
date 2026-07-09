package com.enterprise.marketplace.productservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.InventorySectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductDetailResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.RequestHeaderDto;
import com.enterprise.marketplace.productservice.dto.canonical.RequestInfoDto;
import com.enterprise.marketplace.productservice.dto.canonical.SellerSectionDto;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
class ProductCrudIntegrationTest {

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
    void shouldCreateAndRetrieveProductWithCanonicalEnvelope() {
        UUID sellerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        String sku = "INT-VALVE-" + System.currentTimeMillis();

        CanonicalProductRequest request = CanonicalProductRequest.builder()
                .header(RequestHeaderDto.builder()
                        .sourceSystem("integration-test")
                        .channel("API")
                        .locale("en-IN")
                        .build())
                .requestInfo(RequestInfoDto.builder()
                        .clientRequestId(UUID.randomUUID().toString())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .requestedBy("test-user")
                        .build())
                .seller(SellerSectionDto.builder().sellerId(sellerId).build())
                .product(ProductSectionDto.builder()
                        .sku(sku)
                        .name("Industrial Ball Valve")
                        .description("High pressure ball valve")
                        .categoryId(categoryId)
                        .unitOfMeasure("PCS")
                        .status(ProductStatus.ACTIVE)
                        .build())
                .pricing(PricingSectionDto.builder()
                        .unitPrice(new BigDecimal("2499.50"))
                        .currency("INR")
                        .minQuantity(10)
                        .validFrom(Instant.now())
                        .build())
                .inventory(InventorySectionDto.builder()
                        .quantityAvailable(100)
                        .quantityReserved(0)
                        .reorderLevel(10)
                        .warehouseCode("WH-01")
                        .build())
                .attributes(List.of())
                .media(List.of())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", request.getRequestInfo().getIdempotencyKey());

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<ProductDetailResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/products",
                        HttpMethod.POST,
                        new HttpEntity<>(request, headers),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getSku()).isEqualTo(sku);

        UUID productId = createResponse.getBody().getData().getId();
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<ProductDetailResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/products/" + productId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getName()).isEqualTo("Industrial Ball Valve");
    }
}
