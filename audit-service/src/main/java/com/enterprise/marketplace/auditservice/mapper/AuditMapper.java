package com.enterprise.marketplace.auditservice.mapper;

import com.enterprise.marketplace.auditservice.dto.AuditResponse;
import com.enterprise.marketplace.auditservice.entity.AuditRecordEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditMapper {

    private final ObjectMapper objectMapper;

    public AuditResponse toResponse(AuditRecordEntity entity) {
        return AuditResponse.builder()
                .id(entity.getId())
                .eventKey(entity.getEventKey())
                .requestId(entity.getRequestId())
                .correlationId(entity.getCorrelationId())
                .sourceService(entity.getSourceService())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .operation(entity.getOperation())
                .actor(entity.getActor())
                .beforeState(deserializeJsonMap(entity.getBeforeState()))
                .afterState(deserializeJsonMap(entity.getAfterState()))
                .metadata(deserializeJsonMap(entity.getMetadata()))
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .status(entity.getStatus())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Map<String, Object> deserializeJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
