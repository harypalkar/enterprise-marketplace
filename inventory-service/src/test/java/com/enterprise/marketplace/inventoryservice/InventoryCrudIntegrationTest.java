package com.enterprise.marketplace.inventoryservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.inventoryservice.application.dto.AdjustInventoryQuantityRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.CreateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryResponse;
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
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
            "marketplace.security.enabled=false"
        })
class InventoryCrudIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

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
    void shouldCreateReserveReleaseAndFetchInventory() {
        CreateInventoryRequest request = CreateInventoryRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .quantityAvailable(40)
                .reorderLevel(10)
                .warehouseCode("BLR-01")
                .build();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<InventoryResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/inventory",
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID inventoryId = createResponse.getBody().getData().getId();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<InventoryResponse>> reserveResponse =
                restTemplate.exchange(
                        "/api/v1/inventory/" + inventoryId + "/reserve",
                        HttpMethod.PATCH,
                        new HttpEntity<>(AdjustInventoryQuantityRequest.builder().quantity(15).build()),
                        new ParameterizedTypeReference<>() {});
        assertThat(reserveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reserveResponse.getBody().getData().getQuantityAvailable()).isEqualTo(25);
        assertThat(reserveResponse.getBody().getData().getQuantityReserved()).isEqualTo(15);

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<InventoryResponse>> releaseResponse =
                restTemplate.exchange(
                        "/api/v1/inventory/" + inventoryId + "/release",
                        HttpMethod.PATCH,
                        new HttpEntity<>(AdjustInventoryQuantityRequest.builder().quantity(5).build()),
                        new ParameterizedTypeReference<>() {});
        assertThat(releaseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(releaseResponse.getBody().getData().getQuantityAvailable()).isEqualTo(30);
        assertThat(releaseResponse.getBody().getData().getQuantityReserved()).isEqualTo(10);

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<InventoryResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/inventory/" + inventoryId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getWarehouseCode()).isEqualTo("BLR-01");
    }
}
