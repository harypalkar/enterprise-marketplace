package com.enterprise.marketplace.notificationservice.constants;

public final class NotificationKafkaTopics {

    public static final String NOTIFICATION_CREATED = "notification-created";
    public static final String NOTIFICATION_SENT = "notification-sent";
    public static final String NOTIFICATION_FAILED = "notification-failed";
    public static final String NOTIFICATION_DELIVERED = "notification-delivered";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String WORKFLOW_COMPLETED = "workflow-completed";
    public static final String WORKFLOW_FAILED = "workflow-failed";
    public static final String DEAD_LETTER = "notification-dead-letter";

    private NotificationKafkaTopics() {}
}
