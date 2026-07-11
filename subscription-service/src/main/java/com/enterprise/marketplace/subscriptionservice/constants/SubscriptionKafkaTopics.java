package com.enterprise.marketplace.subscriptionservice.constants;

public final class SubscriptionKafkaTopics {

    public static final String SUBSCRIPTION_CREATED = "subscription-created";
    public static final String SUBSCRIPTION_UPDATED = "subscription-updated";
    public static final String SUBSCRIPTION_CANCELLED = "subscription-cancelled";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String WORKFLOW_COMPLETED = "workflow-completed";
    public static final String DEAD_LETTER = "subscription-dead-letter";

    private SubscriptionKafkaTopics() {}
}
