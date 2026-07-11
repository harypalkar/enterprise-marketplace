package com.enterprise.marketplace.searchservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchOpenApiConfig {

    @Bean
    public GroupedOpenApi searchGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("search-service")
                .pathsToMatch("/api/v1/search/**", "/api/v1/bootstrap/**")
                .displayName("Search Service API")
                .build();
    }
}
