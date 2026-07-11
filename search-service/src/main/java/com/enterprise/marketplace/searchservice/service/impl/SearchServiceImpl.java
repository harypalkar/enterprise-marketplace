package com.enterprise.marketplace.searchservice.service.impl;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.searchservice.audit.SearchAuditService;
import com.enterprise.marketplace.searchservice.constants.SearchCacheKeys;
import com.enterprise.marketplace.searchservice.constants.SearchKafkaTopics;
import com.enterprise.marketplace.searchservice.document.ProductSearchDocument;
import com.enterprise.marketplace.searchservice.dto.ProductSearchPageResponse;
import com.enterprise.marketplace.searchservice.dto.ProductSearchRequest;
import com.enterprise.marketplace.searchservice.dto.ProductSearchResult;
import com.enterprise.marketplace.searchservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.searchservice.entity.SearchSyncLogEntity;
import com.enterprise.marketplace.searchservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.searchservice.enums.SearchAuditOperation;
import com.enterprise.marketplace.searchservice.enums.SyncOperation;
import com.enterprise.marketplace.searchservice.enums.SyncStatus;
import com.enterprise.marketplace.searchservice.redis.SearchCachePort;
import com.enterprise.marketplace.searchservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.searchservice.repository.ProductSearchRepository;
import com.enterprise.marketplace.searchservice.repository.SearchSyncLogRepository;
import com.enterprise.marketplace.searchservice.service.SearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final SearchSyncLogRepository syncLogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final SearchAuditService auditService;
    private final SearchCachePort cachePort;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Override
    public ProductSearchPageResponse searchProducts(ProductSearchRequest request) {
        String cacheKey = SearchCacheKeys.searchQueryKey(buildCacheHash(request));
        ProductSearchPageResponse cached = cachePort.getSearchResult(cacheKey).orElse(null);
        if (cached != null) {
            return cached;
        }

        Criteria criteria = buildSearchCriteria(request);
        Sort sort = parseSort(request.getSort());
        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(PageRequest.of(request.getPage(), request.getSize(), sort));

        SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(query, ProductSearchDocument.class);
        List<ProductSearchResult> content = hits.getSearchHits().stream().map(this::toResult).toList();

        ProductSearchPageResponse response = ProductSearchPageResponse.builder()
                .content(content)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(hits.getTotalHits())
                .totalPages(calculateTotalPages(hits.getTotalHits(), request.getSize()))
                .query(request.getQ())
                .build();

        cachePort.cacheSearchResult(cacheKey, response);
        auditService.record(
                SearchAuditOperation.SEARCH,
                null,
                request.getQ(),
                content.size(),
                Map.of("sellerId", String.valueOf(request.getSellerId()), "categoryId", String.valueOf(request.getCategoryId())));

        log.info("Product search q={} hits={}", request.getQ(), hits.getTotalHits());
        return response;
    }

    @Override
    public ProductSearchResult getProductById(UUID productId) {
        ProductSearchDocument document = productSearchRepository
                .findById(productId.toString())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in search index: " + productId));
        return toResult(document, null);
    }

    @Override
    @Transactional
    public void processIndexEvent(String payload, String eventSource) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = resolveAction(root, eventSource);
            UUID productId = parseProductId(root);
            if (productId == null) {
                log.warn("Skipping {} event without productId", eventSource);
                return;
            }

            auditService.record(
                    SearchAuditOperation.EVENT_RECEIVED,
                    productId,
                    null,
                    null,
                    Map.of("eventSource", eventSource, "action", action));

            if ("DELETE".equalsIgnoreCase(action)) {
                deleteFromIndex(productId, root, payload);
                return;
            }
            indexProduct(productId, root, payload, mapOperation(action));
        } catch (Exception ex) {
            log.error("Failed to process search index event source={}", eventSource, ex);
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Failed to process search index event");
        }
    }

    @Override
    @Transactional
    public ProductSearchResult reindexProduct(UUID productId, String payload) {
        processIndexEvent(payload, "manual-reindex");
        return getProductById(productId);
    }

    private void indexProduct(UUID productId, JsonNode root, String payload, SyncOperation operation) {
        SearchSyncLogEntity syncLog = createSyncLog(productId, operation, payload, root);
        try {
            ProductSearchDocument document = buildDocument(productId, root);
            productSearchRepository.save(document);
            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setProcessedAt(Instant.now());
            syncLogRepository.save(syncLog);

            auditService.record(SearchAuditOperation.INDEX, productId, null, 1, Map.of("operation", operation.name()));
            publishOutbox(productId, "SEARCH_INDEXED", SearchKafkaTopics.SEARCH_INDEXED, buildIndexedPayload(document));
            log.info("Indexed product id={} operation={}", productId, operation);
        } catch (Exception ex) {
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setErrorMessage(truncate(ex.getMessage(), 2000));
            syncLog.setProcessedAt(Instant.now());
            syncLogRepository.save(syncLog);
            publishOutbox(productId, "SEARCH_FAILED", SearchKafkaTopics.SEARCH_FAILED, Map.of("productId", productId.toString(), "error", ex.getMessage()));
            throw ex;
        }
    }

    private void deleteFromIndex(UUID productId, JsonNode root, String payload) {
        SearchSyncLogEntity syncLog = createSyncLog(productId, SyncOperation.DELETE, payload, root);
        try {
            productSearchRepository.deleteById(productId.toString());
            syncLog.setStatus(SyncStatus.SUCCESS);
            syncLog.setProcessedAt(Instant.now());
            syncLogRepository.save(syncLog);
            auditService.record(SearchAuditOperation.DELETE, productId, null, 0, Map.of());
            publishOutbox(productId, "SEARCH_INDEXED", SearchKafkaTopics.SEARCH_INDEXED, Map.of("productId", productId.toString(), "action", "DELETE"));
            log.info("Deleted product from search index id={}", productId);
        } catch (Exception ex) {
            syncLog.setStatus(SyncStatus.FAILED);
            syncLog.setErrorMessage(truncate(ex.getMessage(), 2000));
            syncLog.setProcessedAt(Instant.now());
            syncLogRepository.save(syncLog);
            throw ex;
        }
    }

    private SearchSyncLogEntity createSyncLog(UUID productId, SyncOperation operation, String payload, JsonNode root) {
        SearchSyncLogEntity syncLog = new SearchSyncLogEntity();
        syncLog.setProductId(productId);
        syncLog.setOperation(operation);
        syncLog.setStatus(SyncStatus.PENDING);
        syncLog.setPayload(payload);
        syncLog.setCorrelationId(extractText(root, "correlationId"));
        syncLog.setRequestId(extractText(root, "requestId"));
        return syncLogRepository.save(syncLog);
    }

    private ProductSearchDocument buildDocument(UUID productId, JsonNode root) {
        Instant now = Instant.now();
        return ProductSearchDocument.builder()
                .productId(productId.toString())
                .sku(extractText(root, "sku"))
                .name(extractText(root, "name"))
                .description(extractText(root, "description"))
                .sellerId(extractText(root, "sellerId"))
                .categoryId(extractText(root, "categoryId"))
                .status(extractText(root, "status"))
                .unitPrice(parseDecimal(root, "unitPrice"))
                .currency(extractText(root, "currency"))
                .unitOfMeasure(extractText(root, "unitOfMeasure"))
                .indexedAt(now)
                .updatedAt(now)
                .build();
    }

    private Criteria buildSearchCriteria(ProductSearchRequest request) {
        Criteria criteria = new Criteria();
        if (StringUtils.hasText(request.getQ())) {
            criteria = criteria.and(new Criteria("name").contains(request.getQ())
                    .or(new Criteria("description").contains(request.getQ()))
                    .or(new Criteria("sku").is(request.getQ())));
        }
        if (request.getSellerId() != null) {
            criteria = criteria.and(new Criteria("sellerId").is(request.getSellerId().toString()));
        }
        if (request.getCategoryId() != null) {
            criteria = criteria.and(new Criteria("categoryId").is(request.getCategoryId().toString()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            criteria = criteria.and(new Criteria("status").is(request.getStatus()));
        }
        if (request.getMinPrice() != null) {
            criteria = criteria.and(new Criteria("unitPrice").greaterThanEqual(request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            criteria = criteria.and(new Criteria("unitPrice").lessThanEqual(request.getMaxPrice()));
        }
        return criteria;
    }

    private ProductSearchResult toResult(SearchHit<ProductSearchDocument> hit) {
        Float score = hit.getScore();
        return toResult(hit.getContent(), score != null ? score.doubleValue() : null);
    }

    private ProductSearchResult toResult(ProductSearchDocument document, Double score) {
        return ProductSearchResult.builder()
                .productId(document.getProductId())
                .sku(document.getSku())
                .name(document.getName())
                .description(document.getDescription())
                .sellerId(document.getSellerId())
                .categoryId(document.getCategoryId())
                .status(document.getStatus())
                .unitPrice(document.getUnitPrice())
                .currency(document.getCurrency())
                .unitOfMeasure(document.getUnitOfMeasure())
                .indexedAt(document.getIndexedAt())
                .score(score)
                .build();
    }

    private void publishOutbox(UUID productId, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType("ProductSearch");
        event.setAggregateId(productId);
        event.setEventType(eventType);
        event.setTopic(topic);
        try {
            event.setPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception ex) {
            event.setPayload("{}");
        }
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(outboxMaxRetries);
        event.setCorrelationId(RequestContext.getCorrelationId());
        event.setRequestId(RequestContext.getRequestId());
        outboxEventRepository.save(event);
    }

    private Map<String, Object> buildIndexedPayload(ProductSearchDocument document) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", document.getProductId());
        payload.put("sku", document.getSku());
        payload.put("name", document.getName());
        payload.put("status", document.getStatus());
        payload.put("indexedAt", document.getIndexedAt());
        payload.put("correlationId", RequestContext.getCorrelationId());
        payload.put("requestId", RequestContext.getRequestId());
        return payload;
    }

    private String resolveAction(JsonNode root, String eventSource) {
        String action = extractText(root, "action");
        if (StringUtils.hasText(action)) {
            return action;
        }
        if ("product-deleted".equals(eventSource)) {
            return "DELETE";
        }
        if ("product-updated".equals(eventSource) || "search-index".equals(eventSource)) {
            return "UPDATE";
        }
        return "INDEX";
    }

    private SyncOperation mapOperation(String action) {
        if ("DELETE".equalsIgnoreCase(action)) {
            return SyncOperation.DELETE;
        }
        if ("UPDATE".equalsIgnoreCase(action)) {
            return SyncOperation.UPDATE;
        }
        if ("REINDEX".equalsIgnoreCase(action)) {
            return SyncOperation.REINDEX;
        }
        return SyncOperation.INDEX;
    }

    private UUID parseProductId(JsonNode root) {
        String value = extractText(root, "productId");
        if (!StringUtils.hasText(value)) {
            value = extractText(root, "aggregateId");
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String extractText(JsonNode node, String field) {
        if (node != null && node.hasNonNull(field)) {
            return node.get(field).asText();
        }
        return null;
    }

    private BigDecimal parseDecimal(JsonNode node, String field) {
        if (node != null && node.hasNonNull(field)) {
            return new BigDecimal(node.get(field).asText());
        }
        return null;
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "indexedAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    private int calculateTotalPages(long totalElements, int size) {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    private String buildCacheHash(ProductSearchRequest request) {
        try {
            String raw = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            return String.valueOf(request.hashCode());
        }
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
