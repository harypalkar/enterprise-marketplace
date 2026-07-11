package com.enterprise.marketplace.reportservice.repository;

import com.enterprise.marketplace.reportservice.entity.ReportResultEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportResultRepository extends JpaRepository<ReportResultEntity, UUID> {

    Optional<ReportResultEntity> findByJobId(UUID jobId);
}
