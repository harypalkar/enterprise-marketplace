package com.enterprise.marketplace.gatewayservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for Spring Cloud Gateway (WebFlux).
 */
@Configuration
public class GatewayOpenApiConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI gatewayOpenApi(@Value("${spring.application.name:gateway-service}") String serviceName) {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Marketplace Gateway")
                        .description("API Gateway for " + serviceName)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
