package com.enterprise.marketplace.sellerservice;

import com.enterprise.marketplace.common.util.LoggingUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SellerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SellerServiceApplication.class, args);
        LoggingUtility.setServiceName("seller-service");
    }
}
