package com.enterprise.marketplace.reportservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportOpenApiConfig {

    @Bean
    public GroupedOpenApi reportGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("report-service")
                .pathsToMatch("/api/v1/reports/**")
                .displayName("Report Service API")
                .build();
    }
}
