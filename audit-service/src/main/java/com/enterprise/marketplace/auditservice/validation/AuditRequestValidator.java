package com.enterprise.marketplace.auditservice.validation;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.auditservice.dto.CreateAuditRequest;
import com.enterprise.marketplace.auditservice.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuditRequestValidator {

    private final AuditRecordRepository auditRecordRepository;

    public void validateCreateRequest(CreateAuditRequest request) {
        validateRequiredFields(request);
        if (StringUtils.hasText(request.getEventKey())) {
            validateEventKeyUnique(request.getEventKey());
        }
    }

    public void validateEventKeyUnique(String eventKey) {
        auditRecordRepository.findByEventKey(eventKey).ifPresent(existing -> {
            throw new MarketplaceException(
                    ErrorCode.CONFLICT, "Audit record already exists for eventKey " + eventKey);
        });
    }

    private void validateRequiredFields(CreateAuditRequest request) {
        if (!StringUtils.hasText(request.getRequestId())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "requestId is required");
        }
        if (!StringUtils.hasText(request.getSourceService())) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "sourceService is required");
        }
        if (request.getOperation() == null) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, "operation is required");
        }
    }
}
