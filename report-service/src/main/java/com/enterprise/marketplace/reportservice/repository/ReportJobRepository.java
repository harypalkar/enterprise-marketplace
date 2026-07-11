package com.enterprise.marketplace.reportservice.repository;

import com.enterprise.marketplace.reportservice.entity.ReportJobEntity;
import com.enterprise.marketplace.reportservice.enums.ReportJobStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportJobRepository extends JpaRepository<ReportJobEntity, UUID> {

    Optional<ReportJobEntity> findByIdAndActiveTrue(UUID id);

    Optional<ReportJobEntity> findByRequestIdAndActiveTrue(String requestId);

    Page<ReportJobEntity> findByActiveTrue(Pageable pageable);

    Page<ReportJobEntity> findByReportCodeAndActiveTrue(String reportCode, Pageable pageable);

    Page<ReportJobEntity> findByStatusAndActiveTrue(ReportJobStatus status, Pageable pageable);

    List<ReportJobEntity> findTop20ByStatusAndActiveTrueOrderByCreatedAtAsc(ReportJobStatus status);

    @Query("""
            SELECT j FROM ReportJobEntity j
            WHERE j.active = true
              AND (:reportCode IS NULL OR j.reportCode = :reportCode)
              AND (:status IS NULL OR j.status = :status)
              AND (:requestedBy IS NULL OR j.requestedBy = :requestedBy)
            """)
    Page<ReportJobEntity> searchJobs(
            @Param("reportCode") String reportCode,
            @Param("status") ReportJobStatus status,
            @Param("requestedBy") String requestedBy,
            Pageable pageable);
}
