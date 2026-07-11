package com.enterprise.marketplace.workflowservice.mapper;

import com.enterprise.marketplace.workflowservice.dto.WorkflowHistoryResponse;
import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.entity.WorkflowHistoryEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowMapper {

    private final ObjectMapper objectMapper;

    public WorkflowResponse toResponse(WorkflowEntity entity) {
        return toResponse(entity, Collections.emptyList());
    }

    public WorkflowResponse toResponse(WorkflowEntity entity, List<WorkflowHistoryEntity> history) {
        return WorkflowResponse.builder()
                .id(entity.getId())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .operationType(entity.getOperationType())
                .status(entity.getStatus())
                .previousStatus(entity.getPreviousStatus())
                .tenantId(entity.getTenantId())
                .sourceSystem(entity.getSourceSystem())
                .initiatedBy(entity.getInitiatedBy())
                .message(entity.getMessage())
                .metadata(deserializeMetadata(entity.getMetadata()))
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .history(history.stream().map(this::toHistoryResponse).toList())
                .build();
    }

    public WorkflowHistoryResponse toHistoryResponse(WorkflowHistoryEntity entity) {
        return WorkflowHistoryResponse.builder()
                .id(entity.getId())
                .fromStatus(entity.getFromStatus())
                .toStatus(entity.getToStatus())
                .transitionReason(entity.getTransitionReason())
                .transitionedBy(entity.getTransitionedBy())
                .correlationId(entity.getCorrelationId())
                .requestId(entity.getRequestId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metadata, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
