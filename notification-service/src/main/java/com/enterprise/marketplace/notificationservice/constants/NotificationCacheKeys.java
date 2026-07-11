package com.enterprise.marketplace.notificationservice.constants;

public final class NotificationCacheKeys {

    public static final String TEMPLATE_PREFIX = "notification:template:";
    public static final String NOTIFICATION_PREFIX = "notification:";
    public static final String CHANNEL_PREFIX = "notification:channel:";
    public static final String RATE_LIMIT_PREFIX = "notification:ratelimit:";
    public static final String USER_PREFERENCE_PREFIX = "notification:preference:";

    private NotificationCacheKeys() {}

    public static String templateKey(String templateCode, String channel) {
        return TEMPLATE_PREFIX + templateCode + ":" + channel;
    }

    public static String notificationKey(java.util.UUID notificationId) {
        return NOTIFICATION_PREFIX + notificationId;
    }

    public static String channelKey(String channel) {
        return CHANNEL_PREFIX + channel;
    }

    public static String rateLimitKey(String recipientId, String channel) {
        return RATE_LIMIT_PREFIX + recipientId + ":" + channel;
    }

    public static String userPreferenceKey(String userId) {
        return USER_PREFERENCE_PREFIX + userId;
    }
}
