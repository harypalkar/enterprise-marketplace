package com.enterprise.marketplace.auditservice.repository;

import com.enterprise.marketplace.auditservice.entity.AuditTimelineEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditTimelineRepository extends JpaRepository<AuditTimelineEntity, UUID> {

    List<AuditTimelineEntity> findByCorrelationIdOrderBySequenceNumberAsc(String correlationId);

    @Query("SELECT COALESCE(MAX(t.sequenceNumber), 0) FROM AuditTimelineEntity t WHERE t.correlationId = :correlationId")
    Long findMaxSequenceNumberByCorrelationId(@Param("correlationId") String correlationId);
}
