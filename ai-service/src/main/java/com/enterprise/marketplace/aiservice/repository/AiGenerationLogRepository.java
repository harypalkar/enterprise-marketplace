package com.enterprise.marketplace.aiservice.repository;

import com.enterprise.marketplace.aiservice.entity.AiGenerationLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLogEntity, UUID> {}
