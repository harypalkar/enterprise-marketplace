package com.enterprise.marketplace.identityservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KafkaAdmin.class)
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try {
            kafkaAdmin.initialize();
            return Health.up()
                    .withDetail("component", "kafka")
                    .withDetail("bootstrapServers", kafkaAdmin.getConfigurationProperties().get("bootstrap.servers"))
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).withDetail("component", "kafka").build();
        }
    }
}
