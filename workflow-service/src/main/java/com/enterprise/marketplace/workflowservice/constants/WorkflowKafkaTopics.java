package com.enterprise.marketplace.workflowservice.constants;

public final class WorkflowKafkaTopics {

    public static final String WORKFLOW_CREATED = "workflow-created";
    public static final String WORKFLOW_UPDATED = "workflow-updated";
    public static final String WORKFLOW_COMPLETED = "workflow-completed";
    public static final String WORKFLOW_FAILED = "workflow-failed";
    public static final String WORKFLOW_CANCELLED = "workflow-cancelled";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String NOTIFICATION_CREATED = "notification-created";
    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_UPDATED = "product-updated";
    public static final String DEAD_LETTER = "workflow-dead-letter";

    private WorkflowKafkaTopics() {}
}
