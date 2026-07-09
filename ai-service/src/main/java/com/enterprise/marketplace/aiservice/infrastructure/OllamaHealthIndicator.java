package com.enterprise.marketplace.aiservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OllamaHealthIndicator implements HealthIndicator {

    private final WebClient ollamaWebClient;

    @Value("${marketplace.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Override
    public Health health() {
        try {
            String response = ollamaWebClient
                    .get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return Health.up()
                    .withDetail("component", "ollama")
                    .withDetail("baseUrl", baseUrl)
                    .withDetail("responseLength", response != null ? response.length() : 0)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("component", "ollama").withDetail("baseUrl", baseUrl).build();
        }
    }
}
