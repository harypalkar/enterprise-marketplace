package com.enterprise.marketplace.reportservice.service.impl;



import com.enterprise.marketplace.common.constant.MarketplaceRoles;

import com.enterprise.marketplace.common.context.RequestContext;

import com.enterprise.marketplace.common.exception.ErrorCode;

import com.enterprise.marketplace.common.exception.MarketplaceException;

import com.enterprise.marketplace.common.exception.ResourceNotFoundException;

import com.enterprise.marketplace.reportservice.audit.ReportAuditService;

import com.enterprise.marketplace.reportservice.config.ReportProperties;

import com.enterprise.marketplace.reportservice.constants.ReportKafkaTopics;

import com.enterprise.marketplace.reportservice.dto.CreateReportJobRequest;

import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobPageResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;

import com.enterprise.marketplace.reportservice.dto.ReportJobSearchRequest;

import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;

import com.enterprise.marketplace.reportservice.engine.ReportDataGenerator;

import com.enterprise.marketplace.reportservice.engine.ReportEventBuffer;

import com.enterprise.marketplace.reportservice.entity.OutboxEventEntity;

import com.enterprise.marketplace.reportservice.entity.ReportDefinitionEntity;

import com.enterprise.marketplace.reportservice.entity.ReportJobEntity;

import com.enterprise.marketplace.reportservice.entity.ReportResultEntity;

import com.enterprise.marketplace.reportservice.enums.OutboxEventStatus;

import com.enterprise.marketplace.reportservice.enums.ReportAuditOperation;

import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;

import com.enterprise.marketplace.reportservice.mapper.ReportMapper;

import com.enterprise.marketplace.reportservice.redis.ReportCachePort;

import com.enterprise.marketplace.reportservice.repository.OutboxEventRepository;

import com.enterprise.marketplace.reportservice.repository.ReportDefinitionRepository;

import com.enterprise.marketplace.reportservice.repository.ReportJobRepository;

import com.enterprise.marketplace.reportservice.repository.ReportResultRepository;

import com.enterprise.marketplace.reportservice.service.ReportService;

import com.enterprise.marketplace.reportservice.validation.ReportRequestValidator;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;



@Slf4j

@Service

@RequiredArgsConstructor

@Transactional(readOnly = true)

public class ReportServiceImpl implements ReportService {



    private final ReportJobRepository jobRepository;

    private final ReportDefinitionRepository definitionRepository;

    private final ReportResultRepository resultRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final ReportMapper reportMapper;

    private final ReportRequestValidator requestValidator;

    private final ReportCachePort cachePort;

    private final ReportProperties reportProperties;

    private final ReportAuditService reportAuditService;

    private final ReportDataGenerator reportDataGenerator;

    private final ReportEventBuffer eventBuffer;

    private final ObjectMapper objectMapper;



    @Value("${marketplace.outbox.max-retries:5}")

    private int outboxMaxRetries;



    @Value("${marketplace.security.enabled:true}")

    private boolean securityEnabled;



    @Override

    @Transactional

    public ReportJobResponse createJob(CreateReportJobRequest request) {

        ReportDefinitionEntity definition = requestValidator.validateCreateJobRequest(request);

        applyRequestContext(request);



        ReportJobEntity job = new ReportJobEntity();

        job.setRequestId(request.getRequestId().trim());

        job.setReportCode(definition.getReportCode());

        job.setRequestedBy(resolveRequestedBy(request.getRequestedBy()));

        job.setStatus(ReportJobStatus.PENDING);

        job.setParameters(serializeJson(request.getParameters()));

        job.setActive(true);



        ReportJobEntity saved = jobRepository.save(job);

        reportAuditService.recordAudit(

                saved,

                ReportAuditOperation.CREATE,

                null,

                ReportJobStatus.PENDING,

                null,

                Map.of("reportCode", saved.getReportCode()),

                saved.getRequestedBy());



        ReportJobResponse response = reportMapper.toJobResponse(saved);

        cachePort.cacheJob(response);

        cachePort.evictDefinitions();

        log.info("Report job created id={} requestId={} reportCode={}", saved.getId(), saved.getRequestId(), saved.getReportCode());

        return response;

    }



    @Override

    public ReportJobResponse getJob(UUID jobId) {

        return cachePort.getJob(jobId).orElseGet(() -> {

            ReportJobEntity job = findActiveJob(jobId);

            ReportJobResponse response = reportMapper.toJobResponse(job);

            cachePort.cacheJob(response);

            return response;

        });

    }



    @Override

    public ReportJobPageResponse listJobs(ReportJobSearchRequest request) {

        int page = Math.max(request.getPage(), 0);

        int size = normalizePageSize(request.getSize());

        Page<ReportJobEntity> result = jobRepository.searchJobs(

                request.getReportCode(), request.getStatus(), request.getRequestedBy(), pageable(page, size));

        return toPageResponse(result);

    }



    @Override

    public ReportResultResponse getJobResult(UUID jobId) {

        ReportJobEntity job = findActiveJob(jobId);

        if (job.getStatus() != ReportJobStatus.COMPLETED) {

            throw new MarketplaceException(

                    ErrorCode.VALIDATION_ERROR,

                    "Report result is not available for job in status " + job.getStatus());

        }

        return cachePort.getResult(jobId).orElseGet(() -> {

            ReportResultEntity result = resultRepository

                    .findByJobId(jobId)

                    .orElseThrow(() -> new ResourceNotFoundException("Report result not found for job " + jobId));

            ReportResultResponse response = reportMapper.toResultResponse(result);

            cachePort.cacheResult(response);

            return response;

        });

    }



    @Override

    @Transactional

    public void cancelJob(UUID jobId) {

        ensureAdminRole();

        ReportJobEntity job = findActiveJob(jobId);

        if (job.getStatus() == ReportJobStatus.PROCESSING) {

            throw new MarketplaceException(ErrorCode.CONFLICT, "Cannot cancel job while processing");

        }

        if (job.getStatus() == ReportJobStatus.CANCELLED) {

            return;

        }



        ReportJobStatus previousStatus = job.getStatus();

        job.setStatus(ReportJobStatus.CANCELLED);

        job.setActive(false);

        job.setCompletedAt(Instant.now());

        ReportJobEntity saved = jobRepository.save(job);



        reportAuditService.recordAudit(

                saved,

                ReportAuditOperation.CANCEL,

                previousStatus,

                ReportJobStatus.CANCELLED,

                null,

                null,

                resolveActor(null));



        cachePort.evictJob(jobId);

        cachePort.evictResult(jobId);

        log.info("Report job cancelled id={} requestId={}", jobId, saved.getRequestId());

    }



    @Override

    public List<ReportDefinitionResponse> listDefinitions() {

        return cachePort.getAllDefinitions().orElseGet(() -> {

            List<ReportDefinitionResponse> definitions = definitionRepository.findByActiveTrueOrderByReportCodeAsc().stream()

                    .map(reportMapper::toDefinitionResponse)

                    .toList();

            cachePort.cacheDefinitions(definitions);

            definitions.forEach(cachePort::cacheDefinition);

            return definitions;

        });

    }



    @Override

    public ReportDefinitionResponse getDefinition(String reportCode) {

        return cachePort.getDefinition(reportCode).orElseGet(() -> {

            ReportDefinitionEntity definition = definitionRepository

                    .findByReportCodeAndActiveTrue(reportCode)

                    .orElseThrow(() -> new ResourceNotFoundException("Report definition not found: " + reportCode));

            ReportDefinitionResponse response = reportMapper.toDefinitionResponse(definition);

            cachePort.cacheDefinition(response);

            return response;

        });

    }



    @Override

    @Transactional

    public void processExternalEvent(String payload, String eventSource) {

        try {

            JsonNode root = objectMapper.readTree(payload);

            Map<String, Object> eventMap = objectMapper.convertValue(root, Map.class);



            reportAuditService.recordAudit(

                    null,

                    ReportAuditOperation.EVENT_RECEIVED,

                    null,

                    null,

                    null,

                    eventMap,

                    resolveKafkaActor(root));



            if ("workflow-completed".equals(eventSource)) {

                eventBuffer.recordWorkflowCompleted(eventMap);

                if (reportProperties.isAutoTriggerOnEvents()) {

                    maybeAutoCreateJob("WORKFLOW_STATUS", root, eventMap);

                }

            } else if ("product-created".equals(eventSource)) {

                eventBuffer.recordProductCreated(eventMap);

                if (reportProperties.isAutoTriggerOnEvents()) {

                    maybeAutoCreateJob("INVENTORY_SNAPSHOT", root, eventMap);

                }

            }



            log.info("Processed external event source={} requestId={}", eventSource, extractText(root, "requestId"));

        } catch (Exception ex) {

            log.error("Failed to process external event {}", eventSource, ex);

            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to process external event: " + eventSource);

        }

    }



    @Override

    @Transactional

    public void processPendingJob(UUID jobId) {

        ReportJobEntity job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Report job not found: " + jobId));

        if (job.getStatus() != ReportJobStatus.PENDING || !Boolean.TRUE.equals(job.getActive())) {

            return;

        }

        executeJob(job);

    }



    @Override
    @Transactional
    public void processNextPendingJobs() {

        List<ReportJobEntity> pendingJobs =

                jobRepository.findTop20ByStatusAndActiveTrueOrderByCreatedAtAsc(ReportJobStatus.PENDING);

        for (ReportJobEntity job : pendingJobs) {

            executeJob(job);

        }

    }



    private void executeJob(ReportJobEntity job) {

        ReportJobStatus previousStatus = job.getStatus();

        job.setStatus(ReportJobStatus.PROCESSING);

        job.setStartedAt(Instant.now());

        jobRepository.save(job);



        reportAuditService.recordAudit(

                job,

                ReportAuditOperation.GENERATE,

                previousStatus,

                ReportJobStatus.PROCESSING,

                null,

                null,

                "report-engine");



        try {

            ReportDefinitionEntity definition = definitionRepository

                    .findByReportCodeAndActiveTrue(job.getReportCode())

                    .orElseThrow(() -> new ResourceNotFoundException("Report definition not found: " + job.getReportCode()));



            Map<String, Object> parameters = reportDataGenerator.parseParameters(job.getParameters());

            ReportDataGenerator.GeneratedReportData generated = reportDataGenerator.generate(definition, parameters);



            ReportResultEntity result = new ReportResultEntity();

            result.setJobId(job.getId());

            result.setResultData(serializePayload(generated.resultData()));

            result.setRowCount(generated.rowCount());

            result.setFileUrl(buildFileUrl(job));

            resultRepository.save(result);



            job.setStatus(ReportJobStatus.COMPLETED);

            job.setCompletedAt(Instant.now());

            job.setErrorMessage(null);

            jobRepository.save(job);



            reportAuditService.recordAudit(

                    job,

                    ReportAuditOperation.STATUS_CHANGE,

                    ReportJobStatus.PROCESSING,

                    ReportJobStatus.COMPLETED,

                    null,

                    Map.of("rowCount", generated.rowCount()),

                    "report-engine");



            saveOutboxEvent(job, "REPORT_GENERATED", ReportKafkaTopics.REPORT_GENERATED, buildGeneratedPayload(job, result));

            saveOutboxEvent(job, "AUDIT_CREATED", ReportKafkaTopics.AUDIT_CREATED, buildAuditPayload(job, "GENERATE"));



            cachePort.cacheJob(reportMapper.toJobResponse(job));

            cachePort.cacheResult(reportMapper.toResultResponse(result));



            log.info("Report job completed id={} reportCode={} rowCount={}", job.getId(), job.getReportCode(), generated.rowCount());

        } catch (Exception ex) {

            failJob(job, ex.getMessage());

        }

    }



    private void failJob(ReportJobEntity job, String errorMessage) {

        job.setStatus(ReportJobStatus.FAILED);

        job.setCompletedAt(Instant.now());

        job.setErrorMessage(truncate(errorMessage, 2000));

        jobRepository.save(job);



        reportAuditService.recordAudit(

                job,

                ReportAuditOperation.STATUS_CHANGE,

                ReportJobStatus.PROCESSING,

                ReportJobStatus.FAILED,

                null,

                Map.of("error", job.getErrorMessage()),

                "report-engine");



        saveOutboxEvent(job, "REPORT_FAILED", ReportKafkaTopics.REPORT_FAILED, buildFailedPayload(job));

        saveOutboxEvent(job, "AUDIT_CREATED", ReportKafkaTopics.AUDIT_CREATED, buildAuditPayload(job, "GENERATE"));



        cachePort.cacheJob(reportMapper.toJobResponse(job));

        cachePort.evictResult(job.getId());

        log.error("Report job failed id={} reportCode={} error={}", job.getId(), job.getReportCode(), job.getErrorMessage());

    }



    private void maybeAutoCreateJob(String reportCode, JsonNode root, Map<String, Object> eventMap) {

        String requestId = extractText(root, "requestId");

        if (!StringUtils.hasText(requestId)) {

            requestId = "auto-" + reportCode.toLowerCase() + "-" + UUID.randomUUID();

        } else {

            requestId = "auto-" + reportCode.toLowerCase() + "-" + requestId;

        }



        if (jobRepository.findByRequestIdAndActiveTrue(requestId).isPresent()) {

            return;

        }



        if (definitionRepository.findByReportCodeAndActiveTrue(reportCode).isEmpty()) {

            return;

        }



        CreateReportJobRequest autoRequest = CreateReportJobRequest.builder()

                .requestId(requestId)

                .reportCode(reportCode)

                .requestedBy("event-trigger")

                .correlationId(extractText(root, "correlationId"))

                .parameters(buildAutoParameters(reportCode, eventMap))

                .build();



        try {

            createJob(autoRequest);

            log.info("Auto-created report job reportCode={} requestId={}", reportCode, requestId);

        } catch (MarketplaceException ex) {

            if (ex.getErrorCode() != ErrorCode.CONFLICT) {

                log.warn("Failed to auto-create report job reportCode={}: {}", reportCode, ex.getMessage());

            }

        }

    }



    private Map<String, Object> buildAutoParameters(String reportCode, Map<String, Object> eventMap) {

        Map<String, Object> parameters = new HashMap<>();

        if ("WORKFLOW_STATUS".equals(reportCode)) {

            parameters.put("asOfDate", Instant.now().toString());

            if (eventMap.containsKey("aggregateType")) {

                parameters.put("aggregateType", eventMap.get("aggregateType"));

            }

        } else if ("INVENTORY_SNAPSHOT".equals(reportCode)) {

            parameters.put("snapshotDate", Instant.now().toString().substring(0, 10));

        }

        return parameters;

    }



    private void saveOutboxEvent(ReportJobEntity job, String eventType, String topic, Map<String, Object> payload) {

        OutboxEventEntity event = new OutboxEventEntity();

        event.setAggregateType("ReportJob");

        event.setAggregateId(job.getId());

        event.setEventType(eventType);

        event.setTopic(topic);

        event.setPayload(serializePayload(payload));

        event.setStatus(OutboxEventStatus.PENDING);

        event.setRetryCount(0);

        event.setMaxRetries(outboxMaxRetries);

        event.setCorrelationId(RequestContext.getCorrelationId());

        event.setRequestId(job.getRequestId());

        outboxEventRepository.save(event);

    }



    private Map<String, Object> buildGeneratedPayload(ReportJobEntity job, ReportResultEntity result) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("jobId", job.getId().toString());

        payload.put("requestId", job.getRequestId());

        payload.put("reportCode", job.getReportCode());

        payload.put("requestedBy", job.getRequestedBy());

        payload.put("status", job.getStatus().name());

        payload.put("rowCount", result.getRowCount());

        payload.put("fileUrl", result.getFileUrl());

        payload.put("completedAt", job.getCompletedAt() != null ? job.getCompletedAt().toString() : null);

        return payload;

    }



    private Map<String, Object> buildFailedPayload(ReportJobEntity job) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("jobId", job.getId().toString());

        payload.put("requestId", job.getRequestId());

        payload.put("reportCode", job.getReportCode());

        payload.put("requestedBy", job.getRequestedBy());

        payload.put("status", job.getStatus().name());

        payload.put("errorMessage", job.getErrorMessage());

        payload.put("completedAt", job.getCompletedAt() != null ? job.getCompletedAt().toString() : null);

        return payload;

    }



    private Map<String, Object> buildAuditPayload(ReportJobEntity job, String operation) {

        Map<String, Object> payload = new HashMap<>();

        payload.put("sourceService", "report-service");

        payload.put("requestId", job.getRequestId());

        payload.put("correlationId", RequestContext.getCorrelationId());

        payload.put("aggregateType", "ReportJob");

        payload.put("aggregateId", job.getId().toString());

        payload.put("entityType", "Report");

        payload.put("entityId", job.getId().toString());

        payload.put("operation", operation);

        payload.put("status", job.getStatus().name());

        payload.put("actor", job.getRequestedBy());

        payload.put("afterState", Map.of("reportCode", job.getReportCode(), "status", job.getStatus().name()));

        return payload;

    }



    private String buildFileUrl(ReportJobEntity job) {

        return "/api/v1/reports/jobs/" + job.getId() + "/result";

    }



    private ReportJobEntity findActiveJob(UUID jobId) {

        return jobRepository

                .findByIdAndActiveTrue(jobId)

                .orElseThrow(() -> new ResourceNotFoundException("Report job not found: " + jobId));

    }



    private ReportJobPageResponse toPageResponse(Page<ReportJobEntity> result) {

        List<ReportJobResponse> content = result.getContent().stream().map(reportMapper::toJobResponse).toList();

        return ReportJobPageResponse.builder()

                .content(content)

                .page(result.getNumber())

                .size(result.getSize())

                .totalElements(result.getTotalElements())

                .totalPages(result.getTotalPages())

                .build();

    }



    private PageRequest pageable(int page, int size) {

        return PageRequest.of(page, normalizePageSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));

    }



    private int normalizePageSize(int size) {

        if (size <= 0) {

            return reportProperties.getDefaultPageSize();

        }

        return Math.min(size, reportProperties.getMaxPageSize());

    }



    private void applyRequestContext(CreateReportJobRequest request) {

        if (StringUtils.hasText(request.getCorrelationId())) {

            RequestContext.setCorrelationId(request.getCorrelationId());

        }

    }



    private String resolveRequestedBy(String explicit) {

        return resolveActor(explicit);

    }



    private String resolveActor(String explicit) {

        if (StringUtils.hasText(explicit)) {

            return explicit;

        }

        return RequestContext.getUserId().orElse("system");

    }



    private String resolveKafkaActor(JsonNode root) {

        String actor = extractText(root, "actor");

        if (StringUtils.hasText(actor)) {

            return actor;

        }

        actor = extractText(root, "initiatedBy");

        if (StringUtils.hasText(actor)) {

            return actor;

        }

        return "kafka";

    }



    private void ensureAdminRole() {

        if (!securityEnabled) {

            return;

        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {

            throw new MarketplaceException(ErrorCode.FORBIDDEN, "Cancel requires ADMIN role");

        }

        String adminAuthority = MarketplaceRoles.ROLE_PREFIX + MarketplaceRoles.ADMIN;

        boolean isAdmin = authentication.getAuthorities().stream()

                .map(GrantedAuthority::getAuthority)

                .anyMatch(adminAuthority::equals);

        if (!isAdmin) {

            throw new MarketplaceException(ErrorCode.FORBIDDEN, "Cancel requires ADMIN role");

        }

    }



    private String serializeJson(Map<String, Object> value) {

        if (value == null || value.isEmpty()) {

            return null;

        }

        try {

            return objectMapper.writeValueAsString(value);

        } catch (Exception ex) {

            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "Invalid JSON field value");

        }

    }



    private String serializePayload(Map<String, Object> payload) {

        try {

            return objectMapper.writeValueAsString(payload);

        } catch (Exception ex) {

            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to serialize outbox payload");

        }

    }



    private String extractText(JsonNode node, String field) {

        if (node != null && node.hasNonNull(field)) {

            return node.get(field).asText();

        }

        return null;

    }



    private String truncate(String value, int maxLength) {

        if (value == null) {

            return null;

        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);

    }

}

