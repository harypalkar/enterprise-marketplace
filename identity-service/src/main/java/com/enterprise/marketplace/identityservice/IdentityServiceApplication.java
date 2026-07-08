package com.enterprise.marketplace.identityservice;

import com.enterprise.marketplace.common.util.LoggingUtility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
        LoggingUtility.setServiceName("identity-service");
    }
}
