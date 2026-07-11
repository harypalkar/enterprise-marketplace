package com.enterprise.marketplace.subscriptionservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SubscriptionProperties.class)
public class SubscriptionPropertiesConfig {}
