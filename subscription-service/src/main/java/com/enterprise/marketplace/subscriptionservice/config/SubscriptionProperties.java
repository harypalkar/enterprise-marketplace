package com.enterprise.marketplace.subscriptionservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.subscription")
public class SubscriptionProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
}
