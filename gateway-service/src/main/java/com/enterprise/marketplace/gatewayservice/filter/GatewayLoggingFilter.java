package com.enterprise.marketplace.gatewayservice.filter;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Structured request/response logging at the gateway edge.
 */
@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getRawPath();
        String correlationId = request.getHeaders().getFirst(GatewayCorrelationFilter.CORRELATION_ID_HEADER);
        String requestId = request.getHeaders().getFirst(GatewayCorrelationFilter.REQUEST_ID_HEADER);

        log.info(
                "Gateway request started method={} path={} correlationId={} requestId={}",
                method,
                path,
                correlationId,
                requestId);

        return chain.filter(exchange)
                .doOnSuccess(done -> log.info(
                        "Gateway request completed method={} path={} status={} durationMs={} correlationId={} requestId={}",
                        method,
                        path,
                        exchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - start,
                        correlationId,
                        requestId))
                .doOnError(error -> log.error(
                        "Gateway request failed method={} path={} correlationId={} requestId={}",
                        method,
                        path,
                        correlationId,
                        requestId,
                        error));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
