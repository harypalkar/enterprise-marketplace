package com.enterprise.marketplace.adminservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminOpenApiConfig {

    @Bean
    public GroupedOpenApi adminGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("admin-service")
                .pathsToMatch("/api/v1/admin/**", "/api/v1/bootstrap/**")
                .displayName("Admin Service API")
                .build();
    }
}
