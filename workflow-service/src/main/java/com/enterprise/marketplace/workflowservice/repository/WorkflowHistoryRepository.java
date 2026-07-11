package com.enterprise.marketplace.workflowservice.repository;

import com.enterprise.marketplace.workflowservice.entity.WorkflowHistoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistoryEntity, UUID> {

    List<WorkflowHistoryEntity> findByWorkflowIdOrderByCreatedAtAsc(UUID workflowId);
}
