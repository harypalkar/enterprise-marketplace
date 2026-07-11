package com.enterprise.marketplace.searchservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "marketplace.search")
public class SearchProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
}
