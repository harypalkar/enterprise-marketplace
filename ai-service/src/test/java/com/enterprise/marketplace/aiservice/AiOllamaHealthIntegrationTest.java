package com.enterprise.marketplace.aiservice;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class AiOllamaHealthIntegrationTest {

    private static MockWebServer mockOllama;

    @BeforeAll
    static void startMockOllama() throws IOException {
        mockOllama = new MockWebServer();
        mockOllama.start();
        mockOllama.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"models\":[]}"));
    }

    @AfterAll
    static void stopMockOllama() throws IOException {
        if (mockOllama != null) {
            mockOllama.shutdown();
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("marketplace.ollama.base-url", () -> mockOllama.url("/").toString().replaceAll("/$", ""));
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void ollamaHealthEndpointReturnsUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/v1/infrastructure/health/ollama", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
