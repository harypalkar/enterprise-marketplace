package com.enterprise.marketplace.subscriptionservice.audit;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionAuditEntity;
import com.enterprise.marketplace.subscriptionservice.entity.SubscriptionEntity;
import com.enterprise.marketplace.subscriptionservice.enums.AuditOperation;
import com.enterprise.marketplace.subscriptionservice.enums.SubscriptionStatus;
import com.enterprise.marketplace.subscriptionservice.repository.SubscriptionAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionAuditService {

    private final SubscriptionAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void recordAudit(
            SubscriptionEntity subscription,
            AuditOperation operation,
            SubscriptionStatus beforeStatus,
            SubscriptionStatus afterStatus,
            Object beforeState,
            Object afterState,
            String actor) {
        SubscriptionAuditEntity audit = new SubscriptionAuditEntity();
        audit.setSubscriptionId(subscription.getId());
        audit.setOperation(operation);
        audit.setActor(actor);
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(subscription.getRequestId());
        audit.setBeforeStatus(beforeStatus);
        audit.setAfterStatus(afterStatus);
        audit.setBeforeState(serialize(beforeState));
        audit.setAfterState(serialize(afterState));
        auditRepository.save(audit);
        log.info(
                "Subscription audit recorded subscriptionId={} operation={} before={} after={}",
                subscription.getId(),
                operation,
                beforeStatus,
                afterStatus);
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("Failed to serialize audit state", ex);
            return null;
        }
    }
}
