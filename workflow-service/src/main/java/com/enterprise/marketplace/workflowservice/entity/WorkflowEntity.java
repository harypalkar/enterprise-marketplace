package com.enterprise.marketplace.workflowservice.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.workflowservice.enums.AggregateType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowOperationType;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workflow")
@Getter
@Setter
public class WorkflowEntity extends BaseEntity {

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false, length = 64)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 32)
    private WorkflowOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WorkflowStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private WorkflowStatus previousStatus;

    @Column(name = "tenant_id", length = 64)
    private String tenantId;

    @Column(name = "source_system", length = 64)
    private String sourceSystem;

    @Column(name = "initiated_by", length = 128)
    private String initiatedBy;

    @Column(name = "message", length = 2000)
    private String message;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
