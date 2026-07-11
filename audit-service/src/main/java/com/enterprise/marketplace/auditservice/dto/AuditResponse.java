package com.enterprise.marketplace.auditservice.dto;

import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import com.enterprise.marketplace.auditservice.enums.AuditStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {

    private UUID id;
    private String eventKey;
    private String requestId;
    private String correlationId;
    private String sourceService;
    private String aggregateType;
    private UUID aggregateId;
    private String entityType;
    private UUID entityId;
    private AuditOperation operation;
    private String actor;
    private Map<String, Object> beforeState;
    private Map<String, Object> afterState;
    private Map<String, Object> metadata;
    private String ipAddress;
    private String userAgent;
    private AuditStatus status;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
