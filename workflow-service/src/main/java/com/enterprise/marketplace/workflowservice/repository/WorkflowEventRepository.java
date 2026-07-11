package com.enterprise.marketplace.workflowservice.repository;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEventRepository extends JpaRepository<WorkflowEventEntity, UUID> {

    List<WorkflowEventEntity> findByWorkflowIdOrderByCreatedAtDesc(UUID workflowId);
}
