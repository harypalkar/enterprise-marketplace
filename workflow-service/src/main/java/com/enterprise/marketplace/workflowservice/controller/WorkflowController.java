package com.enterprise.marketplace.workflowservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.workflowservice.dto.CreateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.workflowservice.dto.UpdateWorkflowRequest;
import com.enterprise.marketplace.workflowservice.dto.WorkflowPageResponse;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflows", description = "Enterprise workflow lifecycle APIs")
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    @Idempotent
    @Operation(
            summary = "Create workflow",
            description = "Creates a new workflow record. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request) {
        WorkflowResponse response = workflowService.createWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Workflow created successfully"));
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Get workflow by ID")
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflow(@PathVariable UUID workflowId) {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflow(workflowId)));
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Search workflow by request ID")
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowByRequestId(@PathVariable String requestId) {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflowByRequestId(requestId)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Search workflows by status")
    public ResponseEntity<ApiResponse<WorkflowPageResponse>> getWorkflowsByStatus(
            @PathVariable WorkflowStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(workflowService.getWorkflowsByStatus(status, page, size)));
    }

    @PutMapping("/{workflowId}")
    @Idempotent
    @Operation(summary = "Update workflow metadata")
    public ResponseEntity<ApiResponse<WorkflowResponse>> updateWorkflow(
            @PathVariable UUID workflowId, @Valid @RequestBody UpdateWorkflowRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(workflowService.updateWorkflow(workflowId, request), "Workflow updated successfully"));
    }

    @PatchMapping("/{workflowId}/status")
    @Idempotent
    @Operation(summary = "Update workflow status")
    public ResponseEntity<ApiResponse<WorkflowResponse>> updateStatus(
            @PathVariable UUID workflowId, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                workflowService.updateStatus(workflowId, request), "Workflow status updated successfully"));
    }

    @DeleteMapping("/{workflowId}")
    @Operation(summary = "Soft delete workflow")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflow(@PathVariable UUID workflowId) {
        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow deleted successfully"));
    }
}
