package com.enterprise.marketplace.notificationservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "marketplace.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxSchedulingConfig {}
