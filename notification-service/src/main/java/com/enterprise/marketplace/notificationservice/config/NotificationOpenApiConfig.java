package com.enterprise.marketplace.notificationservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationOpenApiConfig {

    @Bean
    public GroupedOpenApi notificationGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("notification-service")
                .pathsToMatch("/api/v1/notifications/**", "/api/v1/inbox/**", "/api/v1/bootstrap/**")
                .displayName("Notification Service API")
                .build();
    }
}
