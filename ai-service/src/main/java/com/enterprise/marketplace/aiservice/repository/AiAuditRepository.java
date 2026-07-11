package com.enterprise.marketplace.aiservice.repository;

import com.enterprise.marketplace.aiservice.entity.AiAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAuditRepository extends JpaRepository<AiAuditEntity, UUID> {}
