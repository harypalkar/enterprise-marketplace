package com.enterprise.marketplace.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared OpenAPI 3 configuration for servlet-based microservices.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI marketplaceOpenApi(
            @Value("${spring.application.name:marketplace-service}") String serviceName,
            @Value("${marketplace.api.version:v1}") String apiVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Marketplace - " + serviceName)
                        .description("Production-ready B2B marketplace platform API")
                        .version(apiVersion)
                        .contact(new Contact().name("Platform Team").email("platform@marketplace.local"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Keycloak JWT access token")));
    }
}
