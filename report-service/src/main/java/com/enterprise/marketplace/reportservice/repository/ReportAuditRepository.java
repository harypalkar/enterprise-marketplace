package com.enterprise.marketplace.reportservice.repository;

import com.enterprise.marketplace.reportservice.entity.ReportAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportAuditRepository extends JpaRepository<ReportAuditEntity, UUID> {}
