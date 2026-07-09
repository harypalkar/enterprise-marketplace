package com.enterprise.marketplace.aiservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaClientConfig {

    @Bean
    public WebClient ollamaWebClient(@Value("${marketplace.ollama.base-url:http://localhost:11434}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
