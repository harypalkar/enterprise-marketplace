package com.enterprise.marketplace.reportservice.service;



import com.enterprise.marketplace.reportservice.dto.CreateReportJobRequest;

import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobPageResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobSearchRequest;

import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;

import java.util.List;

import java.util.UUID;



public interface ReportService {



    ReportJobResponse createJob(CreateReportJobRequest request);



    ReportJobResponse getJob(UUID jobId);



    ReportJobPageResponse listJobs(ReportJobSearchRequest request);



    ReportResultResponse getJobResult(UUID jobId);



    void cancelJob(UUID jobId);



    List<ReportDefinitionResponse> listDefinitions();



    ReportDefinitionResponse getDefinition(String reportCode);



    void processExternalEvent(String payload, String eventSource);



    void processPendingJob(UUID jobId);

    void processNextPendingJobs();
}

