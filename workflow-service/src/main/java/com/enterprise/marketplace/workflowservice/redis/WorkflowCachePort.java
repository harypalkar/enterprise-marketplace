package com.enterprise.marketplace.workflowservice.redis;

import com.enterprise.marketplace.workflowservice.dto.WorkflowResponse;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import java.util.UUID;

public interface WorkflowCachePort {

    void cacheWorkflow(WorkflowResponse response);

    void evictWorkflow(UUID workflowId);

    void refreshTransitionRules();

    boolean isTransitionAllowed(WorkflowStatus fromStatus, WorkflowStatus toStatus);
}
