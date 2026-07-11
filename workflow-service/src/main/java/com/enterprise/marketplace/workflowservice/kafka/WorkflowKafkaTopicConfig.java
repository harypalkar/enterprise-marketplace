package com.enterprise.marketplace.workflowservice.kafka;

import com.enterprise.marketplace.workflowservice.constants.WorkflowKafkaTopics;
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
public class WorkflowKafkaTopicConfig {

    @Bean
    public NewTopic workflowCreatedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.WORKFLOW_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowUpdatedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.WORKFLOW_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowCompletedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.WORKFLOW_COMPLETED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowFailedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.WORKFLOW_FAILED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowCancelledTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.WORKFLOW_CANCELLED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditCreatedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.AUDIT_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.NOTIFICATION_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowDeadLetterTopic() {
        return TopicBuilder.name(WorkflowKafkaTopics.DEAD_LETTER).partitions(3).replicas(1).build();
    }
}
