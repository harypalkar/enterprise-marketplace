package com.enterprise.marketplace.notificationservice.constants;

public final class NotificationKafkaTopics {

    public static final String NOTIFICATION_CREATED = "notification-created";
    public static final String NOTIFICATION_SENT = "notification-sent";
    public static final String NOTIFICATION_FAILED = "notification-failed";
    public static final String NOTIFICATION_RETRY = "notification-retry";
    public static final String NOTIFICATION_DELIVERED = "notification-delivered";
    public static final String AUDIT_CREATED = "audit-created";
    public static final String WORKFLOW_COMPLETED = "workflow-completed";
    public static final String WORKFLOW_FAILED = "workflow-failed";
    public static final String PRODUCT_CREATED = "product-created";
    public static final String PRODUCT_UPDATED = "product-updated";
    public static final String SELLER_APPROVED = "seller-approved";
    public static final String BUYER_REGISTERED = "buyer-registered";
    public static final String INVENTORY_LOW = "inventory-low";
    public static final String SUBSCRIPTION_EXPIRED = "subscription-expired";
    public static final String DEAD_LETTER = "notification-dead-letter";

    private NotificationKafkaTopics() {}
}
