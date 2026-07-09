package com.enterprise.marketplace.gatewayservice.filter;

import com.enterprise.marketplace.common.constant.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates JWT via Spring Security and propagates identity claims to downstream services.
 */
@Component
public class GatewayJwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    if (securityContext.getAuthentication() != null
                            && securityContext.getAuthentication().getPrincipal() instanceof Jwt jwt) {
                        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
                        builder.header(HttpHeaders.USER_ID, jwt.getSubject());
                        if (jwt.hasClaim("email")) {
                            builder.header("X-User-Email", jwt.getClaimAsString("email"));
                        }
                        if (jwt.hasClaim("realm_access")) {
                            Object roles = jwt.getClaim("realm_access");
                            builder.header("X-User-Roles", String.valueOf(roles));
                        }
                        return chain.filter(exchange.mutate().request(builder.build()).build());
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
