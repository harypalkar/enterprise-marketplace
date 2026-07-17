package com.enterprise.marketplace.gatewayservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Reactive security configuration for API Gateway JWT validation.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http, @Value("${marketplace.security.enabled:true}") boolean securityEnabled) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);

        if (securityEnabled) {
            http.authorizeExchange(exchanges -> exchanges
                            .pathMatchers(
                                    "/actuator/**",
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/webjars/**",
                                    "/api/v1/bootstrap/**",
                                    "/api/identity/api/v1/auth/otp/**",
                                    "/api/identity/api/v1/auth/pin/**",
                                    "/api/identity/api/v1/auth/user/**",
                                    "/api/identity/api/v1/auth/qr/**",
                                    "/api/identity/api/v1/bootstrap/**")
                            .permitAll()
                            .anyExchange()
                            .authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        } else {
            http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        }

        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "marketplace.security.enabled", havingValue = "true", matchIfMissing = true)
    ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
