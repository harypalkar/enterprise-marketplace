package com.enterprise.marketplace.reportservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.report")
public class ReportProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private boolean generationEnabled = true;
    private long processIntervalMs = 5000;
    private boolean autoTriggerOnEvents = true;
}
