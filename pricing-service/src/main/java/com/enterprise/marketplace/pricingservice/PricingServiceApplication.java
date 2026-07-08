package com.enterprise.marketplace.pricingservice;

import com.enterprise.marketplace.common.util.LoggingUtility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PricingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingServiceApplication.class, args);
        LoggingUtility.setServiceName("pricing-service");
    }
}
