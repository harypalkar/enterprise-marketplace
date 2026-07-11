package com.enterprise.marketplace.subscriptionservice.kafka;

import com.enterprise.marketplace.subscriptionservice.constants.SubscriptionKafkaTopics;
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
public class SubscriptionKafkaTopicConfig {

    @Bean
    public NewTopic subscriptionCreatedTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.SUBSCRIPTION_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionUpdatedTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.SUBSCRIPTION_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionCancelledTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.SUBSCRIPTION_CANCELLED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowCompletedTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.WORKFLOW_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic subscriptionDeadLetterTopic() {
        return TopicBuilder.name(SubscriptionKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}
