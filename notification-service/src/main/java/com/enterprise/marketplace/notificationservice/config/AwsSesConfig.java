package com.enterprise.marketplace.notificationservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@ConditionalOnProperty(prefix = "marketplace.notification", name = "email-provider", havingValue = "SES")
public class AwsSesConfig {

    @Bean
    SesClient sesClient(NotificationProperties properties) {
        return SesClient.builder()
                .region(Region.of(properties.getAwsRegion()))
                .build();
    }
}
