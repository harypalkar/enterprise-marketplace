package com.enterprise.marketplace.productservice.service.impl;

import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.productservice.constants.ProductKafkaTopics;
import com.enterprise.marketplace.productservice.dto.canonical.AttributeSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.InventorySectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.MediaSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductDetailResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductPageResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSearchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.RequestInfoDto;
import com.enterprise.marketplace.productservice.dto.canonical.SellerSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.WorkflowSectionDto;
import com.enterprise.marketplace.productservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.productservice.entity.ProductAttributeEntity;
import com.enterprise.marketplace.productservice.entity.ProductAuditEntity;
import com.enterprise.marketplace.productservice.entity.ProductDocumentEntity;
import com.enterprise.marketplace.productservice.entity.ProductEntity;
import com.enterprise.marketplace.productservice.entity.ProductImageEntity;
import com.enterprise.marketplace.productservice.entity.ProductInventoryEntity;
import com.enterprise.marketplace.productservice.entity.ProductPriceEntity;
import com.enterprise.marketplace.productservice.entity.ProductWorkflowEntity;
import com.enterprise.marketplace.productservice.enums.AuditOperation;
import com.enterprise.marketplace.productservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.enterprise.marketplace.productservice.enums.ProductWorkflowStatus;
import com.enterprise.marketplace.productservice.mapper.ProductMapper;
import com.enterprise.marketplace.productservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.productservice.repository.ProductAttributeRepository;
import com.enterprise.marketplace.productservice.repository.ProductAuditRepository;
import com.enterprise.marketplace.productservice.repository.ProductDocumentRepository;
import com.enterprise.marketplace.productservice.repository.ProductImageRepository;
import com.enterprise.marketplace.productservice.repository.ProductInventoryRepository;
import com.enterprise.marketplace.productservice.repository.ProductPriceRepository;
import com.enterprise.marketplace.productservice.repository.ProductRepository;
import com.enterprise.marketplace.productservice.repository.ProductSpecifications;
import com.enterprise.marketplace.productservice.repository.ProductWorkflowRepository;
import com.enterprise.marketplace.productservice.service.ProductService;
import com.enterprise.marketplace.productservice.validation.ProductValidationContext;
import com.enterprise.marketplace.productservice.validation.ProductValidationPipeline;
import com.enterprise.marketplace.productservice.validation.ValidationOperation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final String AGGREGATE_TYPE = "Product";

    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductInventoryRepository productInventoryRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductWorkflowRepository productWorkflowRepository;
    private final ProductAuditRepository productAuditRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductValidationPipeline validationPipeline;
    private final ObjectMapper objectMapper;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductPriceRepository productPriceRepository,
            ProductInventoryRepository productInventoryRepository,
            ProductAttributeRepository productAttributeRepository,
            ProductImageRepository productImageRepository,
            ProductDocumentRepository productDocumentRepository,
            ProductWorkflowRepository productWorkflowRepository,
            ProductAuditRepository productAuditRepository,
            OutboxEventRepository outboxEventRepository,
            ProductValidationPipeline validationPipeline,
            ObjectMapper objectMapper,
            @Qualifier("canonicalProductMapperImpl") ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productInventoryRepository = productInventoryRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.productImageRepository = productImageRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.productWorkflowRepository = productWorkflowRepository;
        this.productAuditRepository = productAuditRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.validationPipeline = validationPipeline;
        this.objectMapper = objectMapper;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(CanonicalProductRequest request) {
        applyRequestContext(request.getRequestInfo(), request.getHeader().getTenantId());
        validationPipeline.validate(new ProductValidationContext(request, ValidationOperation.CREATE, null));

        ProductEntity product = productMapper.toEntity(request);
        ProductEntity savedProduct = productRepository.save(product);

        ProductPriceEntity price = savePrice(savedProduct.getId(), request.getPricing());
        ProductInventoryEntity inventory = saveInventory(savedProduct.getId(), request.getInventory());
        List<ProductAttributeEntity> attributes = saveAttributes(savedProduct.getId(), request.getAttributes());
        List<ProductImageEntity> images = saveImages(savedProduct.getId(), request.getMedia());

        ProductWorkflowEntity workflow = newWorkflow(savedProduct.getId(), ProductWorkflowStatus.INITIAL, null);
        transitionWorkflow(workflow, ProductWorkflowStatus.VALIDATING, "Starting validation");
        transitionWorkflow(workflow, ProductWorkflowStatus.BUSINESS_VALIDATED, "Validation complete");
        transitionWorkflow(workflow, ProductWorkflowStatus.PERSISTED, "Product persisted");

        Map<String, String> tags = extractTags(request.getMetadata());
        saveAudit(savedProduct.getId(), AuditOperation.CREATE, null, savedProduct, request.getRequestInfo());
        saveOutboxEvent(
                savedProduct.getId(),
                "product-created",
                ProductKafkaTopics.PRODUCT_CREATED,
                buildEventPayload(savedProduct, "CREATED"));
        saveOutboxEvent(
                savedProduct.getId(),
                "search-index",
                ProductKafkaTopics.SEARCH_INDEX,
                buildSearchIndexPayload(savedProduct, "INDEX"));
        transitionWorkflow(workflow, ProductWorkflowStatus.OUTBOX_CREATED, "Outbox event created");
        transitionWorkflow(
                workflow,
                resolveTargetWorkflowStatus(request.getWorkflow(), ProductWorkflowStatus.COMPLETED),
                workflowMessage(request.getWorkflow()));
        productWorkflowRepository.save(workflow);

        return productMapper.toDetailResponse(
                savedProduct, price, inventory, attributes, images, Collections.emptyList(), workflow, tags);
    }

    @Override
    public ProductDetailResponse getProduct(UUID id) {
        ProductEntity product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return buildDetailResponse(product);
    }

    @Override
    public ProductPageResponse listProducts(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<ProductEntity> result = productRepository.findAll(pageable);
        return toPageResponse(result);
    }

    @Override
    public ProductPageResponse searchProducts(ProductSearchRequest request) {
        Specification<ProductEntity> spec = ProductSpecifications.withCriteria(
                request.getStatus(), request.getSellerId(), request.getCategoryId(), request.getKeyword());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), parseSort(request.getSort()));
        Page<ProductEntity> result = productRepository.findAll(spec, pageable);
        return toPageResponse(result);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(UUID id, CanonicalProductRequest request) {
        applyRequestContext(request.getRequestInfo(), request.getHeader().getTenantId());
        ProductEntity existing = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        ensureNotArchived(existing);

        validationPipeline.validate(new ProductValidationContext(request, ValidationOperation.UPDATE, id));

        String beforeState = serialize(existing);
        ProductEntity updated = productMapper.toEntity(request);
        updated.setId(existing.getId());
        updated.setVersion(existing.getVersion());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        ProductEntity savedProduct = productRepository.save(updated);

        productPriceRepository.deleteByProductId(id);
        productAttributeRepository.deleteByProductId(id);
        productImageRepository.deleteByProductId(id);

        ProductPriceEntity price = savePrice(id, request.getPricing());
        ProductInventoryEntity inventory = updateOrCreateInventory(id, request.getInventory());
        List<ProductAttributeEntity> attributes = saveAttributes(id, request.getAttributes());
        List<ProductImageEntity> images = saveImages(id, request.getMedia());
        ProductWorkflowEntity workflow = updateWorkflow(id, request.getWorkflow());

        saveAudit(id, AuditOperation.UPDATE, beforeState, savedProduct, request.getRequestInfo());
        saveOutboxEvent(id, "product-updated", ProductKafkaTopics.PRODUCT_UPDATED, buildEventPayload(savedProduct, "UPDATED"));
        saveOutboxEvent(id, "search-index", ProductKafkaTopics.SEARCH_INDEX, buildSearchIndexPayload(savedProduct, "UPDATE"));

        return productMapper.toDetailResponse(
                savedProduct,
                price,
                inventory,
                attributes,
                images,
                productDocumentRepository.findByProductId(id),
                workflow,
                extractTags(request.getMetadata()));
    }

    @Override
    @Transactional
    public ProductDetailResponse patchProduct(UUID id, CanonicalProductPatchRequest request) {
        if (request.getRequestInfo() != null || request.getHeader() != null) {
            applyRequestContext(
                    request.getRequestInfo(),
                    request.getHeader() != null ? request.getHeader().getTenantId() : null);
        }
        ProductEntity existing = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        ensureNotArchived(existing);

        validationPipeline.validate(new ProductValidationContext(request, ValidationOperation.PATCH, id));

        String beforeState = serialize(existing);
        patchProductEntity(existing, request);
        ProductEntity savedProduct = productRepository.save(existing);

        ProductPriceEntity price = productPriceRepository
                .findFirstByProductIdOrderByValidFromDesc(id)
                .orElse(null);
        if (request.getPricing() != null) {
            productPriceRepository.deleteByProductId(id);
            price = savePrice(id, request.getPricing());
        }

        ProductInventoryEntity inventory = productInventoryRepository.findByProductId(id).orElse(null);
        if (request.getInventory() != null) {
            inventory = updateOrCreateInventory(id, request.getInventory());
        }

        List<ProductAttributeEntity> attributes = productAttributeRepository.findByProductId(id);
        if (request.getAttributes() != null) {
            productAttributeRepository.deleteByProductId(id);
            attributes = saveAttributes(id, request.getAttributes());
        }

        List<ProductImageEntity> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(id);
        if (request.getMedia() != null) {
            productImageRepository.deleteByProductId(id);
            images = saveImages(id, request.getMedia());
        }

        ProductWorkflowEntity workflow = updateWorkflow(id, request.getWorkflow());
        saveAudit(id, AuditOperation.PATCH, beforeState, savedProduct, request.getRequestInfo());
        saveOutboxEvent(id, "product-updated", ProductKafkaTopics.PRODUCT_UPDATED, buildEventPayload(savedProduct, "PATCHED"));
        saveOutboxEvent(id, "search-index", ProductKafkaTopics.SEARCH_INDEX, buildSearchIndexPayload(savedProduct, "UPDATE"));

        return productMapper.toDetailResponse(
                savedProduct,
                price,
                inventory,
                attributes,
                images,
                productDocumentRepository.findByProductId(id),
                workflow,
                request.getMetadata() != null ? extractTags(request.getMetadata()) : null);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        ProductEntity existing = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (existing.getStatus() == ProductStatus.ARCHIVED) {
            return;
        }

        String beforeState = serialize(existing);
        existing.setStatus(ProductStatus.ARCHIVED);
        ProductEntity savedProduct = productRepository.save(existing);

        ProductWorkflowEntity workflow = productWorkflowRepository
                .findByProductId(id)
                .orElseGet(() -> newWorkflow(id, ProductWorkflowStatus.INITIAL, null));
        transitionWorkflow(workflow, ProductWorkflowStatus.CANCELLED, "Product archived");
        productWorkflowRepository.save(workflow);

        saveAudit(id, AuditOperation.DELETE, beforeState, savedProduct, null);
        saveOutboxEvent(id, "product-deleted", ProductKafkaTopics.PRODUCT_DELETED, buildEventPayload(savedProduct, "DELETED"));
        saveOutboxEvent(id, "search-index", ProductKafkaTopics.SEARCH_INDEX, buildSearchIndexPayload(savedProduct, "DELETE"));
    }

    private ProductDetailResponse buildDetailResponse(ProductEntity product) {
        UUID productId = product.getId();
        return productMapper.toDetailResponse(
                product,
                productPriceRepository.findFirstByProductIdOrderByValidFromDesc(productId).orElse(null),
                productInventoryRepository.findByProductId(productId).orElse(null),
                productAttributeRepository.findByProductId(productId),
                productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId),
                productDocumentRepository.findByProductId(productId),
                productWorkflowRepository.findByProductId(productId).orElse(null),
                null);
    }

    private ProductPageResponse toPageResponse(Page<ProductEntity> page) {
        List<ProductDetailResponse> content = page.getContent().stream()
                .map(this::buildDetailResponse)
                .toList();
        return ProductPageResponse.builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .build();
    }

    private ProductPriceEntity savePrice(UUID productId, PricingSectionDto pricing) {
        ProductPriceEntity price = new ProductPriceEntity();
        price.setProductId(productId);
        price.setUnitPrice(pricing.getUnitPrice());
        price.setCurrency(pricing.getCurrency().toUpperCase());
        price.setMinQuantity(pricing.getMinQuantity() != null ? pricing.getMinQuantity() : 1);
        price.setDiscountPercent(pricing.getDiscountPercent());
        price.setValidFrom(pricing.getValidFrom() != null ? pricing.getValidFrom() : Instant.now());
        price.setValidTo(pricing.getValidTo());
        return productPriceRepository.save(price);
    }

    private ProductInventoryEntity saveInventory(UUID productId, InventorySectionDto inventory) {
        ProductInventoryEntity entity = new ProductInventoryEntity();
        entity.setProductId(productId);
        entity.setQuantityAvailable(defaultInt(inventory.getQuantityAvailable(), 0));
        entity.setQuantityReserved(defaultInt(inventory.getQuantityReserved(), 0));
        entity.setReorderLevel(defaultInt(inventory.getReorderLevel(), 0));
        entity.setWarehouseCode(inventory.getWarehouseCode());
        return productInventoryRepository.save(entity);
    }

    private ProductInventoryEntity updateOrCreateInventory(UUID productId, InventorySectionDto inventory) {
        return productInventoryRepository
                .findByProductId(productId)
                .map(existing -> {
                    existing.setQuantityAvailable(defaultInt(inventory.getQuantityAvailable(), existing.getQuantityAvailable()));
                    existing.setQuantityReserved(defaultInt(inventory.getQuantityReserved(), existing.getQuantityReserved()));
                    existing.setReorderLevel(defaultInt(inventory.getReorderLevel(), existing.getReorderLevel()));
                    if (inventory.getWarehouseCode() != null) {
                        existing.setWarehouseCode(inventory.getWarehouseCode());
                    }
                    return productInventoryRepository.save(existing);
                })
                .orElseGet(() -> saveInventory(productId, inventory));
    }

    private List<ProductAttributeEntity> saveAttributes(UUID productId, List<AttributeSectionDto> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Collections.emptyList();
        }
        return attributes.stream()
                .map(dto -> {
                    ProductAttributeEntity entity = new ProductAttributeEntity();
                    entity.setProductId(productId);
                    entity.setAttrKey(dto.getKey());
                    entity.setAttrValue(dto.getValue());
                    return productAttributeRepository.save(entity);
                })
                .toList();
    }

    private List<ProductImageEntity> saveImages(UUID productId, List<MediaSectionDto> media) {
        if (media == null || media.isEmpty()) {
            return Collections.emptyList();
        }
        int order = 0;
        List<ProductImageEntity> saved = new java.util.ArrayList<>();
        for (MediaSectionDto dto : media) {
            ProductImageEntity entity = new ProductImageEntity();
            entity.setProductId(productId);
            entity.setUrl(dto.getUrl());
            entity.setAltText(dto.getAltText());
            entity.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : order++);
            entity.setPrimaryImage(Boolean.TRUE.equals(dto.getPrimaryImage()));
            saved.add(productImageRepository.save(entity));
        }
        return saved;
    }

    private ProductWorkflowEntity updateWorkflow(UUID productId, WorkflowSectionDto workflowSection) {
        ProductWorkflowEntity workflow = productWorkflowRepository
                .findByProductId(productId)
                .orElseGet(() -> newWorkflow(productId, ProductWorkflowStatus.INITIAL, null));
        transitionWorkflow(
                workflow,
                resolveTargetWorkflowStatus(workflowSection, ProductWorkflowStatus.AMENDED),
                workflowMessage(workflowSection));
        return productWorkflowRepository.save(workflow);
    }

    private ProductWorkflowEntity newWorkflow(UUID productId, ProductWorkflowStatus status, String message) {
        ProductWorkflowEntity workflow = new ProductWorkflowEntity();
        workflow.setProductId(productId);
        workflow.setStatus(status);
        workflow.setMessage(message);
        return workflow;
    }

    private void transitionWorkflow(
            ProductWorkflowEntity workflow, ProductWorkflowStatus targetStatus, String message) {
        workflow.setPreviousStatus(workflow.getStatus());
        workflow.setStatus(targetStatus);
        if (StringUtils.hasText(message)) {
            workflow.setMessage(message);
        }
    }

    private ProductWorkflowStatus resolveTargetWorkflowStatus(
            WorkflowSectionDto workflowSection, ProductWorkflowStatus defaultStatus) {
        if (workflowSection != null && workflowSection.getTargetStatus() != null) {
            return workflowSection.getTargetStatus();
        }
        return defaultStatus;
    }

    private String workflowMessage(WorkflowSectionDto workflowSection) {
        return workflowSection != null ? workflowSection.getMessage() : null;
    }

    private void saveAudit(
            UUID productId,
            AuditOperation operation,
            String beforeState,
            ProductEntity afterEntity,
            RequestInfoDto requestInfo) {
        ProductAuditEntity audit = new ProductAuditEntity();
        audit.setProductId(productId);
        audit.setOperation(operation);
        audit.setActor(resolveActor(requestInfo));
        audit.setCorrelationId(RequestContext.getCorrelationId());
        audit.setRequestId(RequestContext.getRequestId());
        audit.setBeforeState(beforeState);
        audit.setAfterState(serialize(afterEntity));
        productAuditRepository.save(audit);
    }

    private void saveOutboxEvent(UUID aggregateId, String eventType, String topic, Map<String, Object> payload) {
        OutboxEventEntity event = new OutboxEventEntity();
        event.setAggregateType(AGGREGATE_TYPE);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setPayload(serialize(payload));
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(0);
        event.setMaxRetries(5);
        event.setCorrelationId(RequestContext.getCorrelationId());
        event.setRequestId(RequestContext.getRequestId());
        outboxEventRepository.save(event);
    }

    private Map<String, Object> buildEventPayload(ProductEntity product, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", product.getId());
        payload.put("sku", product.getSku());
        payload.put("sellerId", product.getSellerId());
        payload.put("status", product.getStatus());
        payload.put("action", action);
        payload.put("correlationId", RequestContext.getCorrelationId());
        payload.put("requestId", RequestContext.getRequestId());
        payload.put("occurredAt", Instant.now());
        return payload;
    }

    private Map<String, Object> buildSearchIndexPayload(ProductEntity product, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", product.getId());
        payload.put("sku", product.getSku());
        payload.put("name", product.getName());
        payload.put("description", product.getDescription());
        payload.put("sellerId", product.getSellerId());
        payload.put("categoryId", product.getCategoryId());
        payload.put("unitPrice", product.getUnitPrice());
        payload.put("currency", product.getCurrency());
        payload.put("unitOfMeasure", product.getUnitOfMeasure());
        payload.put("status", product.getStatus());
        payload.put("action", action);
        payload.put("correlationId", RequestContext.getCorrelationId());
        payload.put("requestId", RequestContext.getRequestId());
        payload.put("occurredAt", Instant.now());
        return payload;
    }

    private void patchProductEntity(ProductEntity existing, CanonicalProductPatchRequest request) {
        SellerSectionDto seller = request.getSeller();
        if (seller != null && seller.getSellerId() != null) {
            existing.setSellerId(seller.getSellerId());
        }

        ProductSectionDto product = request.getProduct();
        if (product != null) {
            if (StringUtils.hasText(product.getSku())) {
                existing.setSku(product.getSku());
            }
            if (StringUtils.hasText(product.getName())) {
                existing.setName(product.getName());
            }
            if (product.getDescription() != null) {
                existing.setDescription(product.getDescription());
            }
            if (product.getCategoryId() != null) {
                existing.setCategoryId(product.getCategoryId());
            }
            if (StringUtils.hasText(product.getUnitOfMeasure())) {
                existing.setUnitOfMeasure(product.getUnitOfMeasure());
            }
            if (product.getHsnCode() != null) {
                existing.setHsnCode(product.getHsnCode());
            }
            if (product.getStatus() != null) {
                existing.setStatus(product.getStatus());
            }
        }

        PricingSectionDto pricing = request.getPricing();
        if (pricing != null) {
            if (pricing.getUnitPrice() != null) {
                existing.setUnitPrice(pricing.getUnitPrice());
            }
            if (StringUtils.hasText(pricing.getCurrency())) {
                existing.setCurrency(pricing.getCurrency().toUpperCase());
            }
            if (pricing.getMinQuantity() != null) {
                existing.setMinOrderQuantity(pricing.getMinQuantity());
            }
        }
    }

    private void applyRequestContext(RequestInfoDto requestInfo, String tenantId) {
        if (requestInfo != null) {
            if (StringUtils.hasText(requestInfo.getCorrelationId())) {
                RequestContext.setCorrelationId(requestInfo.getCorrelationId());
            } else if (!StringUtils.hasText(RequestContext.getCorrelationId())) {
                RequestContext.setCorrelationId(RequestContext.generateCorrelationId());
            }
            if (StringUtils.hasText(requestInfo.getRequestId())) {
                RequestContext.setRequestId(requestInfo.getRequestId());
            } else if (!StringUtils.hasText(RequestContext.getRequestId())) {
                RequestContext.setRequestId(RequestContext.generateRequestId());
            }
            if (StringUtils.hasText(requestInfo.getIdempotencyKey())) {
                RequestContext.setIdempotencyKey(requestInfo.getIdempotencyKey());
            }
            if (StringUtils.hasText(requestInfo.getRequestedBy())) {
                RequestContext.setUserId(requestInfo.getRequestedBy());
            }
        }
        if (StringUtils.hasText(tenantId)) {
            RequestContext.setTenantId(tenantId);
        }
    }

    private String resolveActor(RequestInfoDto requestInfo) {
        if (requestInfo != null && StringUtils.hasText(requestInfo.getRequestedBy())) {
            return requestInfo.getRequestedBy();
        }
        return RequestContext.getUserId().orElse("system");
    }

    private void ensureNotArchived(ProductEntity product) {
        if (product.getStatus() == ProductStatus.ARCHIVED) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Archived products cannot be modified");
        }
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction =
                parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    private Map<String, String> extractTags(
            com.enterprise.marketplace.productservice.dto.canonical.MetadataSectionDto metadata) {
        if (metadata == null || metadata.getTags() == null) {
            return null;
        }
        return metadata.getTags();
    }

    private int defaultInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Unable to serialize entity state", ex);
        }
    }
}
