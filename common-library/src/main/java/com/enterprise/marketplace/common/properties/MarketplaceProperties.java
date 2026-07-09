package com.enterprise.marketplace.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shared marketplace platform properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace")
public class MarketplaceProperties {

    private Service service = new Service();
    private Api api = new Api();
    private Security security = new Security();

    @Getter
    @Setter
    public static class Service {
        private String name;
    }

    @Getter
    @Setter
    public static class Api {
        private String version = "v1";
    }

    @Getter
    @Setter
    public static class Security {
        private boolean enabled = true;
    }
}
