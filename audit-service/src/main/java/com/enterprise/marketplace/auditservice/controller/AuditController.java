package com.enterprise.marketplace.auditservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.auditservice.dto.AuditPageResponse;
import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.dto.AuditSearchRequest;
import com.enterprise.marketplace.auditservice.dto.AuditTimelineResponse;
import com.enterprise.marketplace.auditservice.dto.CreateAuditRequest;
import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import com.enterprise.marketplace.auditservice.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audits")
@RequiredArgsConstructor
@Tag(name = "Audits", description = "Enterprise audit record lifecycle APIs")
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    @Idempotent
    @Operation(
            summary = "Create audit record",
            description = "Creates a new immutable audit record. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<AuditResponse>> createAudit(@Valid @RequestBody CreateAuditRequest request) {
        AuditResponse response = auditService.createAudit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Audit record created successfully"));
    }

    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit record by ID")
    public ResponseEntity<ApiResponse<AuditResponse>> getAudit(@PathVariable UUID auditId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAudit(auditId)));
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get latest audit record by request ID")
    public ResponseEntity<ApiResponse<AuditResponse>> getByRequestId(@PathVariable String requestId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getByRequestId(requestId)));
    }

    @GetMapping("/correlation/{correlationId}")
    @Operation(summary = "Get audit timeline by correlation ID")
    public ResponseEntity<ApiResponse<AuditTimelineResponse>> getByCorrelationId(
            @PathVariable String correlationId) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getByCorrelationId(correlationId)));
    }

    @GetMapping("/aggregate")
    @Operation(summary = "Search audit records by aggregate")
    public ResponseEntity<ApiResponse<AuditPageResponse>> getByAggregate(
            @RequestParam String aggregateType,
            @RequestParam UUID aggregateId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(auditService.getByAggregate(aggregateType, aggregateId, page, size)));
    }

    @GetMapping("/actor/{actor}")
    @Operation(summary = "Search audit records by actor")
    public ResponseEntity<ApiResponse<AuditPageResponse>> getByActor(
            @PathVariable String actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getByActor(actor, page, size)));
    }

    @GetMapping("/source/{sourceService}")
    @Operation(summary = "Search audit records by source service")
    public ResponseEntity<ApiResponse<AuditPageResponse>> getBySourceService(
            @PathVariable String sourceService,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditService.getBySourceService(sourceService, page, size)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search audit records with filters")
    public ResponseEntity<ApiResponse<AuditPageResponse>> searchAudits(
            @RequestParam(required = false) AuditOperation operation,
            @RequestParam(required = false) String sourceService,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        AuditSearchRequest request = AuditSearchRequest.builder()
                .operation(operation)
                .sourceService(sourceService)
                .actor(actor)
                .fromDate(fromDate)
                .toDate(toDate)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(ApiResponse.success(auditService.searchAudits(request)));
    }

    @DeleteMapping("/{auditId}")
    @Operation(summary = "Archive audit record (soft delete, ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> archiveAudit(@PathVariable UUID auditId) {
        auditService.archiveAudit(auditId);
        return ResponseEntity.ok(ApiResponse.success(null, "Audit record archived successfully"));
    }
}
