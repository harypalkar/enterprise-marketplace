package com.enterprise.marketplace.reportservice.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "report_job")
@Getter
@Setter
public class ReportJobEntity extends BaseEntity {

    @Column(name = "request_id", nullable = false, unique = true, length = 64)
    private String requestId;

    @Column(name = "report_code", nullable = false, length = 64)
    private String reportCode;

    @Column(name = "requested_by", nullable = false, length = 128)
    private String requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReportJobStatus status;

    @Column(name = "parameters", columnDefinition = "jsonb")
    private String parameters;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
