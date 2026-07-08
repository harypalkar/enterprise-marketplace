package com.enterprise.marketplace.categoryservice;

import com.enterprise.marketplace.common.util.LoggingUtility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CategoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApplication.class, args);
        LoggingUtility.setServiceName("category-service");
    }
}
