package com.enterprise.marketplace.gatewayservice.handler;

import com.enterprise.marketplace.common.api.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for Spring Cloud Gateway (reactive).
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(ex);

        log.warn("Gateway error path={} status={} message={}", exchange.getRequest().getPath(), status.value(), message);

        ErrorResponse body = ErrorResponse.builder()
                .code("ERR-GW-" + status.value())
                .message(message)
                .status(status.value())
                .path(exchange.getRequest().getPath().value())
                .correlationId(exchange.getRequest().getHeaders().getFirst(GatewayHeaderConstants.CORRELATION_ID))
                .requestId(exchange.getRequest().getHeaders().getFirst(GatewayHeaderConstants.REQUEST_ID))
                .timestamp(Instant.now())
                .build();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException jsonEx) {
            byte[] fallback = "{\"code\":\"ERR-GW-500\",\"message\":\"Internal gateway error\"}"
                    .getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(fallback)));
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof InvalidBearerTokenException || ex instanceof AccessDeniedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex) {
        if (ex instanceof InvalidBearerTokenException) {
            return "Invalid or expired JWT token";
        }
        if (ex instanceof AccessDeniedException) {
            return "Access denied";
        }
        if (ex instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getReason() != null
                    ? responseStatusException.getReason()
                    : responseStatusException.getStatusCode().toString();
        }
        return "Gateway processing error";
    }
}
