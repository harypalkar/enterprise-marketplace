package com.enterprise.marketplace.identityservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Keycloak connection and realm configuration properties.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String realm = "marketplace";
    private String authServerUrl = "http://localhost:8180";
    private String clientId = "marketplace-services";
    private String clientSecret = "marketplace-services-secret";

    public String getIssuerUri() {
        return authServerUrl + "/realms/" + realm;
    }

    public String getJwkSetUri() {
        return getIssuerUri() + "/protocol/openid-connect/certs";
    }
}
