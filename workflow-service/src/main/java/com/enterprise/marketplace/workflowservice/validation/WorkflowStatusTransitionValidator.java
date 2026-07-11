package com.enterprise.marketplace.workflowservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.workflowservice.enums.WorkflowStatus;
import com.enterprise.marketplace.workflowservice.redis.WorkflowCachePort;
import com.enterprise.marketplace.workflowservice.repository.WorkflowRepository;
import com.enterprise.marketplace.workflowservice.repository.WorkflowTransitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkflowStatusTransitionValidator {

    private final WorkflowTransitionRepository transitionRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowCachePort cacheService;

    public void validateTransition(WorkflowStatus fromStatus, WorkflowStatus toStatus) {
        if (fromStatus == toStatus) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Workflow is already in status " + toStatus);
        }
        if (!isTransitionAllowed(fromStatus, toStatus)) {
            throw new MarketplaceException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Transition from " + fromStatus + " to " + toStatus + " is not allowed");
        }
    }

    public void validateRequestIdUnique(String requestId) {
        workflowRepository.findByRequestIdAndActiveTrue(requestId).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Workflow already exists for requestId " + requestId);
        });
    }

    private boolean isTransitionAllowed(WorkflowStatus fromStatus, WorkflowStatus toStatus) {
        if (cacheService.isTransitionAllowed(fromStatus, toStatus)) {
            return true;
        }
        return transitionRepository.existsByFromStatusAndToStatusAndActiveTrue(fromStatus, toStatus);
    }
}
