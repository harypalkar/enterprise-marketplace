package com.enterprise.marketplace.auditservice.repository;

import com.enterprise.marketplace.auditservice.entity.AuditEventLogEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventLogRepository extends JpaRepository<AuditEventLogEntity, UUID> {

    List<AuditEventLogEntity> findByAuditRecordId(UUID auditRecordId);

    List<AuditEventLogEntity> findByRequestId(String requestId);

    List<AuditEventLogEntity> findByCorrelationId(String correlationId);

    List<AuditEventLogEntity> findByProcessedFalseOrderByCreatedAtAsc();
}
