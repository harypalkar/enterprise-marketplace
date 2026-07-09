package com.enterprise.marketplace.common.constant;

/**
 * Keycloak realm roles for Enterprise Marketplace Platform.
 */
public final class MarketplaceRoles {

    public static final String ADMIN = "ADMIN";
    public static final String SELLER = "SELLER";
    public static final String BUYER = "BUYER";

    public static final String ROLE_PREFIX = "ROLE_";

    private MarketplaceRoles() {
        throw new UnsupportedOperationException("Utility class");
    }
}
