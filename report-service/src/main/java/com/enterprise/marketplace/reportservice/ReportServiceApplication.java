package com.enterprise.marketplace.reportservice;

import com.enterprise.marketplace.common.util.LoggingUtility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
        LoggingUtility.setServiceName("report-service");
    }
}
