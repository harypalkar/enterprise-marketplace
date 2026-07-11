package com.enterprise.marketplace.subscriptionservice.constants;

import java.util.UUID;

public final class SubscriptionCacheKeys {

    public static final String SUBSCRIPTION_PREFIX = "subscription:";
    public static final String PLAN_PREFIX = "subscription:plan:";
    public static final String PLAN_CODE_PREFIX = "subscription:plan:code:";

    private SubscriptionCacheKeys() {}

    public static String subscriptionKey(UUID subscriptionId) {
        return SUBSCRIPTION_PREFIX + subscriptionId;
    }

    public static String planKey(UUID planId) {
        return PLAN_PREFIX + planId;
    }

    public static String planCodeKey(String planCode) {
        return PLAN_CODE_PREFIX + planCode;
    }
}
