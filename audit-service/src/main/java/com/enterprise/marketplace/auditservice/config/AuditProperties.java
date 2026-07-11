package com.enterprise.marketplace.auditservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.audit")
public class AuditProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
}
