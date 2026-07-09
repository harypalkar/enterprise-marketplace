package com.enterprise.marketplace.identityservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic definitions for platform events.
 */
@Configuration
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(KafkaAdmin.class)
public class KafkaTopicConfig {

    @Bean
    public NewTopic marketplaceEventsTopic(@Value("${marketplace.kafka.topics.events:marketplace.events}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic marketplaceAuditTopic(@Value("${marketplace.kafka.topics.audit:marketplace.audit}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
