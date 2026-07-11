package com.enterprise.marketplace.aiservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiOpenApiConfig {

    @Bean
    public GroupedOpenApi aiGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("ai-service")
                .pathsToMatch("/api/v1/ai/**", "/api/v1/bootstrap/**")
                .displayName("AI Service API")
                .build();
    }
}
