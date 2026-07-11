package com.enterprise.marketplace.adminservice.repository;

import com.enterprise.marketplace.adminservice.entity.AdminAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditRepository extends JpaRepository<AdminAuditEntity, UUID> {}
