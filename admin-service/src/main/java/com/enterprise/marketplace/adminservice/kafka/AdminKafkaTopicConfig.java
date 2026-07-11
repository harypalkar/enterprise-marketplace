package com.enterprise.marketplace.adminservice.kafka;

import com.enterprise.marketplace.adminservice.constants.AdminKafkaTopics;
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
public class AdminKafkaTopicConfig {

    @Bean
    public NewTopic adminConfigChangedTopic() {
        return TopicBuilder.name(AdminKafkaTopics.ADMIN_CONFIG_CHANGED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic adminFeatureToggledTopic() {
        return TopicBuilder.name(AdminKafkaTopics.ADMIN_FEATURE_TOGGLED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(AdminKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic adminDeadLetterTopic() {
        return TopicBuilder.name(AdminKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}
