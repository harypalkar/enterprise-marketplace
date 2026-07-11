package com.enterprise.marketplace.auditservice.dto;

import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateAuditRequest {

    @Size(max = 128)
    private String eventKey;

    @NotBlank
    @Size(max = 64)
    private String requestId;

    @Size(max = 64)
    private String correlationId;

    @NotBlank
    @Size(max = 64)
    private String sourceService;

    @Size(max = 64)
    private String aggregateType;

    private UUID aggregateId;

    @Size(max = 64)
    private String entityType;

    private UUID entityId;

    @NotNull
    private AuditOperation operation;

    @Size(max = 128)
    private String actor;

    private Map<String, Object> beforeState;

    private Map<String, Object> afterState;

    private Map<String, Object> metadata;

    @Size(max = 64)
    private String ipAddress;

    @Size(max = 512)
    private String userAgent;
}
