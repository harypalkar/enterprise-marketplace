package com.enterprise.marketplace.adminservice.constants;

public final class AdminCacheKeys {

    private static final String PREFIX = "admin:";

    private AdminCacheKeys() {}

    public static String settingKey(String key) {
        return PREFIX + "setting:" + key;
    }

    public static String settingsAllKey() {
        return PREFIX + "settings:all";
    }

    public static String featureFlagKey(String key) {
        return PREFIX + "flag:" + key;
    }

    public static String featureFlagsAllKey() {
        return PREFIX + "flags:all";
    }
}
