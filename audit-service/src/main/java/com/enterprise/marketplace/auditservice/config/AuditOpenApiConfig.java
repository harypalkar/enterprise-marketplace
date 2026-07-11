package com.enterprise.marketplace.auditservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditOpenApiConfig {

    @Bean
    public GroupedOpenApi auditGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("audit-service")
                .pathsToMatch("/api/v1/audits/**", "/api/v1/bootstrap/**")
                .displayName("Audit Service API")
                .build();
    }
}
