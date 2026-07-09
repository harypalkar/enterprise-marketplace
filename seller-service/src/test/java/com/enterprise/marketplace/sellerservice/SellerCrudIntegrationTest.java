package com.enterprise.marketplace.sellerservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.sellerservice.application.dto.CreateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.SellerResponse;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
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
class SellerCrudIntegrationTest {

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
    void shouldCreateAndRetrieveSeller() {
        CreateSellerRequest request = CreateSellerRequest.builder()
                .companyName("Harish Exports Private Limited")
                .tradeName("Harish Exports")
                .gstin("27AABCU9603R1ZM")
                .pan("AABCU9603R")
                .email("contact@harishexports.in")
                .phone("9876543210")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .pinCode("400001")
                .status(SellerStatus.ACTIVE)
                .build();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<SellerResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/sellers",
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getData().getGstin()).isEqualTo("27AABCU9603R1ZM");

        var sellerId = createResponse.getBody().getData().getId();
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<SellerResponse>> getResponse =
                restTemplate.exchange(
                        "/api/v1/sellers/" + sellerId,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getData().getCompanyName()).isEqualTo("Harish Exports Private Limited");
    }
}
