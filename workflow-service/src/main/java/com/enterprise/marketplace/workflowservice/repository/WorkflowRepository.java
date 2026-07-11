package com.enterprise.marketplace.workflowservice.repository;

import com.enterprise.marketplace.workflowservice.entity.WorkflowEntity;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity, UUID> {

    Optional<WorkflowEntity> findByIdAndActiveTrue(UUID id);

    Optional<WorkflowEntity> findByRequestIdAndActiveTrue(String requestId);

    Page<WorkflowEntity> findByStatusAndActiveTrue(WorkflowStatus status, Pageable pageable);

    Optional<WorkflowEntity> findByAggregateTypeAndAggregateIdAndActiveTrue(
            com.enterprise.marketplace.workflowservice.enums.AggregateType aggregateType, UUID aggregateId);

    List<WorkflowEntity> findByCorrelationIdAndActiveTrue(String correlationId);
}
