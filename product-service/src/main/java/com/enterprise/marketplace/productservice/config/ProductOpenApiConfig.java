package com.enterprise.marketplace.productservice.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductOpenApiConfig {

    @Bean
    public GroupedOpenApi productGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("product-service")
                .pathsToMatch("/api/v1/products/**", "/api/v1/bootstrap/**")
                .displayName("Product Service API")
                .build();
    }
}
