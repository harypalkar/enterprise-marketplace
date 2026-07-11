package com.enterprise.marketplace.adminservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.enterprise.marketplace.adminservice.dto.CreateSettingRequest;
import com.enterprise.marketplace.adminservice.dto.DashboardResponse;
import com.enterprise.marketplace.adminservice.dto.SettingResponse;
import java.util.List;
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
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "marketplace.security.enabled=false",
    "marketplace.kafka.enabled=false",
    "marketplace.redis.enabled=false",
    "marketplace.outbox.enabled=false"
})
class AdminCrudIntegrationTest {

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
    void shouldListSeededSettingsAndCreateNewSetting() {
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<List<SettingResponse>>> listResponse =
                restTemplate.exchange(
                        "/api/v1/admin/settings",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().getData()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(listResponse.getBody().getData())
                .extracting(SettingResponse::getSettingKey)
                .contains("marketplace.name", "marketplace.default_currency", "feature.ai.enabled");

        CreateSettingRequest request = CreateSettingRequest.builder()
                .settingKey("integration.test.setting")
                .settingValue("enabled")
                .category("TEST")
                .description("Integration test setting")
                .active(true)
                .build();

        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<SettingResponse>> createResponse =
                restTemplate.exchange(
                        "/api/v1/admin/settings",
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        new ParameterizedTypeReference<>() {});

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().getData().getSettingKey()).isEqualTo("integration.test.setting");
    }

    @Test
    void shouldReturnDashboardWithPlatformMetrics() {
        ResponseEntity<com.enterprise.marketplace.common.api.ApiResponse<DashboardResponse>> dashboardResponse =
                restTemplate.exchange(
                        "/api/v1/admin/dashboard",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {});

        assertThat(dashboardResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dashboardResponse.getBody()).isNotNull();
        assertThat(dashboardResponse.getBody().getData().getPlatformMetrics())
                .containsKeys("subscriptions.total", "reports.total", "users.total");
        assertThat(dashboardResponse.getBody().getData().getSettings().getTotal())
                .isGreaterThanOrEqualTo(3);
    }
}
