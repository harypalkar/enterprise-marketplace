package com.enterprise.marketplace.notificationservice.audit;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.notificationservice.entity.NotificationAuditEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.AuditOperation;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.repository.NotificationAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAuditService {

    private final NotificationAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void recordAudit(
            NotificationEntity notification,
            AuditOperation operation,
            NotificationStatus beforeStatus,
            NotificationStatus afterStatus,
            Object beforeState,
            Object afterState,
            String actor) {
        NotificationAuditEntity audit = new NotificationAuditEntity();
        audit.setNotificationId(notification.getId());
        audit.setOperation(operation);
        audit.setActor(actor);
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(notification.getRequestId());
        audit.setBeforeStatus(beforeStatus);
        audit.setAfterStatus(afterStatus);
        audit.setBeforeState(serialize(beforeState));
        audit.setAfterState(serialize(afterState));
        auditRepository.save(audit);
        log.info(
                "Notification audit recorded notificationId={} operation={} before={} after={}",
                notification.getId(),
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
