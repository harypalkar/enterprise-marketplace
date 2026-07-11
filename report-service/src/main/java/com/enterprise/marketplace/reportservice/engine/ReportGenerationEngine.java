package com.enterprise.marketplace.reportservice.engine;



import com.enterprise.marketplace.reportservice.service.ReportService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;



@Slf4j

@Component

@RequiredArgsConstructor

@ConditionalOnProperty(prefix = "marketplace.report", name = "generation-enabled", havingValue = "true", matchIfMissing = true)

public class ReportGenerationEngine {



    private final ReportService reportService;



    @Scheduled(fixedDelayString = "${marketplace.report.process-interval-ms:5000}")

    public void processPendingJobs() {

        try {

            reportService.processNextPendingJobs();

        } catch (Exception ex) {

            log.error("Report generation engine failed", ex);

        }

    }

}

