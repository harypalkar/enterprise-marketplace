package com.enterprise.marketplace.workflowservice.repository;

import com.enterprise.marketplace.workflowservice.entity.WorkflowAuditEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowAuditRepository extends JpaRepository<WorkflowAuditEntity, UUID> {

    List<WorkflowAuditEntity> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}
