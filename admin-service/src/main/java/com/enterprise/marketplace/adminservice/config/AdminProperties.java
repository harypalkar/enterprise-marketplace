package com.enterprise.marketplace.adminservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.admin")
public class AdminProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
}
