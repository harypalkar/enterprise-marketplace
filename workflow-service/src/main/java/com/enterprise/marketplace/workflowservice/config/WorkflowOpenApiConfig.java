package com.enterprise.marketplace.workflowservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowOpenApiConfig {

    @Bean
    public GroupedOpenApi workflowGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("workflow-service")
                .pathsToMatch("/api/v1/workflows/**", "/api/v1/bootstrap/**")
                .displayName("Workflow Service API")
                .build();
    }
}
