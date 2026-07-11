package com.enterprise.marketplace.auditservice.kafka;

import com.enterprise.marketplace.auditservice.constants.AuditKafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(KafkaAdmin.class)
public class AuditKafkaTopicConfig {

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(AuditKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditIndexedTopic() {
        return TopicBuilder.name(AuditKafkaTopics.AUDIT_INDEXED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditArchivedTopic() {
        return TopicBuilder.name(AuditKafkaTopics.AUDIT_ARCHIVED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditDeadLetterTopic() {
        return TopicBuilder.name(AuditKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}
