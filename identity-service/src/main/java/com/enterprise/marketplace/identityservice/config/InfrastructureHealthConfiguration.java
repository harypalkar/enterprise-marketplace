package com.enterprise.marketplace.identityservice.config;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.identityservice.infrastructure.InfrastructureHealthAggregator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@ConditionalOnBean({RedisConnectionFactory.class, KafkaAdmin.class})
@RequiredArgsConstructor
public class InfrastructureHealthConfiguration {

    private final InfrastructureHealthAggregator healthAggregator;

    @Bean
    InfrastructureHealthEndpoints infrastructureHealthEndpoints() {
        return new InfrastructureHealthEndpoints(healthAggregator);
    }

    @RequestMapping("/api/v1/infrastructure")
    static class InfrastructureHealthEndpoints {

        private final InfrastructureHealthAggregator healthAggregator;

        InfrastructureHealthEndpoints(InfrastructureHealthAggregator healthAggregator) {
            this.healthAggregator = healthAggregator;
        }

        @GetMapping("/health")
        @ResponseBody
        ResponseEntity<ApiResponse<Map<String, Object>>> infrastructureHealth() {
            Map<String, Object> payload = healthAggregator.collectHealth();
            return ResponseEntity.ok(ApiResponse.success(payload, "Infrastructure health snapshot"));
        }

        @GetMapping("/health/redis")
        @ResponseBody
        ResponseEntity<ApiResponse<Map<String, Object>>> redisHealth() {
            Map<String, Object> payload = healthAggregator.collectHealth();
            return ResponseEntity.ok(ApiResponse.success(Map.of("redis", payload.get("redis")), "Redis health"));
        }

        @GetMapping("/health/kafka")
        @ResponseBody
        ResponseEntity<ApiResponse<Map<String, Object>>> kafkaHealth() {
            Map<String, Object> payload = healthAggregator.collectHealth();
            return ResponseEntity.ok(ApiResponse.success(Map.of("kafka", payload.get("kafka")), "Kafka health"));
        }
    }
}
