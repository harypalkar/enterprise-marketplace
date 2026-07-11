package com.enterprise.marketplace.notificationservice.constants;

public final class NotificationCacheKeys {

    public static final String TEMPLATE_PREFIX = "notification:template:";
    public static final String NOTIFICATION_PREFIX = "notification:";

    private NotificationCacheKeys() {}

    public static String templateKey(String templateCode, String channel) {
        return TEMPLATE_PREFIX + templateCode + ":" + channel;
    }

    public static String notificationKey(java.util.UUID notificationId) {
        return NOTIFICATION_PREFIX + notificationId;
    }
}
