package com.enterprise.marketplace.buyerservice;

import com.enterprise.marketplace.common.util.LoggingUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BuyerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyerServiceApplication.class, args);
        LoggingUtility.setServiceName("buyer-service");
    }
}
