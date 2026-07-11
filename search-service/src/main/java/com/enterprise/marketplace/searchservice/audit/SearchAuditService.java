package com.enterprise.marketplace.searchservice.audit;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.searchservice.entity.SearchAuditEntity;
import com.enterprise.marketplace.searchservice.enums.SearchAuditOperation;
import com.enterprise.marketplace.searchservice.repository.SearchAuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAuditService {

    private final SearchAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(
            SearchAuditOperation operation,
            UUID productId,
            String queryText,
            Integer resultCount,
            Map<String, Object> metadata) {
        SearchAuditEntity audit = new SearchAuditEntity();
        audit.setOperation(operation);
        audit.setProductId(productId);
        audit.setQueryText(queryText);
        audit.setResultCount(resultCount);
        audit.setActor(RequestContext.getUserId().orElse("system"));
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(RequestContext.getRequestId());
        if (metadata != null && !metadata.isEmpty()) {
            try {
                audit.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception ex) {
                audit.setMetadata("{}");
            }
        }
        auditRepository.save(audit);
    }
}
