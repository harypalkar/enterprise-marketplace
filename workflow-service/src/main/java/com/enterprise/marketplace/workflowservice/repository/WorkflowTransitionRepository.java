package com.enterprise.marketplace.workflowservice.repository;

import com.enterprise.marketplace.workflowservice.entity.WorkflowTransitionEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, UUID> {

    List<WorkflowTransitionEntity> findByActiveTrue();

    boolean existsByFromStatusAndToStatusAndActiveTrue(WorkflowStatus fromStatus, WorkflowStatus toStatus);
}
