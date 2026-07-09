package com.enterprise.marketplace.sellerservice.infrastructure.config;

import com.enterprise.marketplace.common.constant.MarketplaceRoles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = false)
public class SellerSecurityConfig {

    @Bean
    SecurityFilterChain sellerSecurityFilterChain(
            HttpSecurity http, @Value("${marketplace.security.enabled:true}") boolean securityEnabled)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth.requestMatchers(
                                    "/actuator/**",
                                    "/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/api/v1/bootstrap/**")
                            .permitAll()
                            .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/sellers/**")
                            .permitAll()
                            .requestMatchers("/api/v1/sellers/**")
                            .hasAnyRole(MarketplaceRoles.SELLER, MarketplaceRoles.ADMIN)
                            .anyRequest()
                            .authenticated())
                    .oauth2ResourceServer(
                            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                roles.stream()
                        .map(Object::toString)
                        .map(role -> new SimpleGrantedAuthority(MarketplaceRoles.ROLE_PREFIX + role))
                        .forEach(authorities::add);
            }
            return authorities;
        }
    }
}
