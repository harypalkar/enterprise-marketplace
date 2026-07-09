package com.enterprise.marketplace.gatewayservice.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Gateway infrastructure health and metadata endpoints.
 */
@RestController
@RequestMapping("/api/v1/bootstrap")
public class GatewayBootstrapController {

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", serviceName);
        payload.put("status", "UP");
        payload.put("type", "gateway");
        return Mono.just(ResponseEntity.ok(payload));
    }
}
