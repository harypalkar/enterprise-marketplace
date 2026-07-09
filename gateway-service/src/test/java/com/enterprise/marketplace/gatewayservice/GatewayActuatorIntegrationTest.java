package com.enterprise.marketplace.gatewayservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
    "marketplace.security.enabled=false"
})
@AutoConfigureWebTestClient
class GatewayActuatorIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void healthEndpointReturnsUp() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void livenessProbeReturnsUp() {
        webTestClient.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    }

    @Test
    void readinessProbeReturnsUp() {
        webTestClient.get().uri("/actuator/health/readiness").exchange().expectStatus().isOk();
    }

    @Test
    void prometheusEndpointIsAccessible() {
        webTestClient.get().uri("/actuator/prometheus").exchange().expectStatus().isOk();
    }
}
