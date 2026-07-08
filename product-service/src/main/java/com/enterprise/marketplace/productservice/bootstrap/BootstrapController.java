package com.enterprise.marketplace.productservice.bootstrap;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.context.RequestContext;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bootstrap")
public class BootstrapController {

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> payload = Map.of(
                "service", serviceName,
                "status", "UP",
                "correlationId", RequestContext.getCorrelationId(),
                "requestId", RequestContext.getRequestId());
        ApiResponse<Map<String, String>> response = ApiResponse.success(payload, "Service bootstrap health check");
        response.setCorrelationId(RequestContext.getCorrelationId());
        response.setRequestId(RequestContext.getRequestId());
        return ResponseEntity.ok(response);
    }
}
