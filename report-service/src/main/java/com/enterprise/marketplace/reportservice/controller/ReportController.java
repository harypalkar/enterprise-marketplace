package com.enterprise.marketplace.reportservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.reportservice.dto.CreateReportJobRequest;
import com.enterprise.marketplace.reportservice.dto.ReportDefinitionResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobPageResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobResponse;
import com.enterprise.marketplace.reportservice.dto.ReportJobSearchRequest;
import com.enterprise.marketplace.reportservice.dto.ReportResultResponse;
import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import com.enterprise.marketplace.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Enterprise report generation lifecycle APIs")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/jobs")
    @Idempotent
    @Operation(
            summary = "Create report job",
            description = "Queues an asynchronous report generation job. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<ReportJobResponse>> createJob(@Valid @RequestBody CreateReportJobRequest request) {
        ReportJobResponse response = reportService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Report job created successfully"));
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get report job by ID")
    public ResponseEntity<ApiResponse<ReportJobResponse>> getJob(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getJob(jobId)));
    }

    @GetMapping("/jobs")
    @Operation(summary = "List report jobs with optional filters")
    public ResponseEntity<ApiResponse<ReportJobPageResponse>> listJobs(
            @RequestParam(required = false) String reportCode,
            @RequestParam(required = false) ReportJobStatus status,
            @RequestParam(required = false) String requestedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ReportJobSearchRequest request = ReportJobSearchRequest.builder()
                .reportCode(reportCode)
                .status(status)
                .requestedBy(requestedBy)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(ApiResponse.success(reportService.listJobs(request)));
    }

    @GetMapping("/jobs/{jobId}/result")
    @Operation(summary = "Get report result for a completed job")
    public ResponseEntity<ApiResponse<ReportResultResponse>> getJobResult(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getJobResult(jobId)));
    }

    @DeleteMapping("/jobs/{jobId}")
    @Operation(summary = "Cancel report job (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> cancelJob(@PathVariable UUID jobId) {
        reportService.cancelJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(null, "Report job cancelled successfully"));
    }

    @GetMapping("/definitions")
    @Operation(summary = "List active report definitions")
    public ResponseEntity<ApiResponse<List<ReportDefinitionResponse>>> listDefinitions() {
        return ResponseEntity.ok(ApiResponse.success(reportService.listDefinitions()));
    }

    @GetMapping("/definitions/{code}")
    @Operation(summary = "Get report definition by code")
    public ResponseEntity<ApiResponse<ReportDefinitionResponse>> getDefinition(@PathVariable("code") String code) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDefinition(code)));
    }
}
