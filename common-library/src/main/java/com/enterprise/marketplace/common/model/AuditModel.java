package com.enterprise.marketplace.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable audit metadata for entities requiring explicit audit trail columns.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AuditModel {

    @Column(name = "audit_action", length = 64)
    private String action;

    @Column(name = "audit_actor", length = 128)
    private String actor;

    @Column(name = "audit_actor_type", length = 32)
    private String actorType;

    @Column(name = "audit_source", length = 64)
    private String source;

    @Column(name = "audit_correlation_id", length = 64)
    private String correlationId;

    @Column(name = "audit_request_id", length = 64)
    private String requestId;

    @Column(name = "audit_ip_address", length = 45)
    private String ipAddress;

    @Column(name = "audit_user_agent", length = 512)
    private String userAgent;

    @Column(name = "audit_timestamp")
    private Instant timestamp;

    @Column(name = "audit_remarks", length = 1024)
    private String remarks;

    public static AuditModel fromCurrentContext(String action, String actor, String actorType, String source) {
        return AuditModel.builder()
                .action(action)
                .actor(actor)
                .actorType(actorType)
                .source(source)
                .timestamp(Instant.now())
                .build();
    }
}
