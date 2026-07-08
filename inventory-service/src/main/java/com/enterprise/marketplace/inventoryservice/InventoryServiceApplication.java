package com.enterprise.marketplace.inventoryservice;

import com.enterprise.marketplace.common.util.LoggingUtility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
        LoggingUtility.setServiceName("inventory-service");
    }
}
