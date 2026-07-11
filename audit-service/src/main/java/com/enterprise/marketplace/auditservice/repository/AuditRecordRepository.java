package com.enterprise.marketplace.auditservice.repository;

import com.enterprise.marketplace.auditservice.entity.AuditRecordEntity;
import com.enterprise.marketplace.auditservice.enums.AuditOperation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditRecordRepository extends JpaRepository<AuditRecordEntity, UUID> {

    Optional<AuditRecordEntity> findByIdAndActiveTrue(UUID id);

    Optional<AuditRecordEntity> findByEventKey(String eventKey);

    Optional<AuditRecordEntity> findTopByRequestIdAndActiveTrueOrderByCreatedAtDesc(String requestId);

    Page<AuditRecordEntity> findByRequestIdAndActiveTrue(String requestId, Pageable pageable);

    Page<AuditRecordEntity> findBySourceServiceAndActiveTrue(String sourceService, Pageable pageable);

    Page<AuditRecordEntity> findByActorAndActiveTrue(String actor, Pageable pageable);

    Page<AuditRecordEntity> findByAggregateTypeAndAggregateIdAndActiveTrue(
            String aggregateType, UUID aggregateId, Pageable pageable);

    Page<AuditRecordEntity> findByOperationAndActiveTrue(AuditOperation operation, Pageable pageable);

    @Query(
            """
            SELECT a FROM AuditRecordEntity a
            WHERE a.active = true
            AND (:operation IS NULL OR a.operation = :operation)
            AND (:sourceService IS NULL OR a.sourceService = :sourceService)
            AND (:actor IS NULL OR a.actor = :actor)
            AND (:fromDate IS NULL OR a.createdAt >= :fromDate)
            AND (:toDate IS NULL OR a.createdAt <= :toDate)
            """)
    Page<AuditRecordEntity> searchAudits(
            @Param("operation") AuditOperation operation,
            @Param("sourceService") String sourceService,
            @Param("actor") String actor,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable);
}
