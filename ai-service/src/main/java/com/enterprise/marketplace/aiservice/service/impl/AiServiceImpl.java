package com.enterprise.marketplace.aiservice.service.impl;

import com.enterprise.marketplace.aiservice.constants.AiKafkaTopics;
import com.enterprise.marketplace.aiservice.dto.ChatMessageDto;
import com.enterprise.marketplace.aiservice.dto.ChatRequest;
import com.enterprise.marketplace.aiservice.dto.ChatResponse;
import com.enterprise.marketplace.aiservice.dto.GenerateDescriptionRequest;
import com.enterprise.marketplace.aiservice.dto.GenerateDescriptionResponse;
import com.enterprise.marketplace.aiservice.dto.RecommendationResponse;
import com.enterprise.marketplace.aiservice.dto.RecommendedProductDto;
import com.enterprise.marketplace.aiservice.dto.SearchInterpretRequest;
import com.enterprise.marketplace.aiservice.dto.SearchInterpretResponse;
import com.enterprise.marketplace.aiservice.entity.AiAuditEntity;
import com.enterprise.marketplace.aiservice.entity.AiChatMessageEntity;
import com.enterprise.marketplace.aiservice.entity.AiChatSessionEntity;
import com.enterprise.marketplace.aiservice.entity.AiGenerationLogEntity;
import com.enterprise.marketplace.aiservice.entity.AiPromptTemplateEntity;
import com.enterprise.marketplace.aiservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.aiservice.enums.AiAuditOperation;
import com.enterprise.marketplace.aiservice.enums.AiChatRole;
import com.enterprise.marketplace.aiservice.enums.AiGenerationStatus;
import com.enterprise.marketplace.aiservice.enums.AiUseCase;
import com.enterprise.marketplace.aiservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.aiservice.provider.OllamaProvider;
import com.enterprise.marketplace.aiservice.provider.OllamaProvider.OllamaMessage;
import com.enterprise.marketplace.aiservice.provider.OllamaProvider.OllamaResult;
import com.enterprise.marketplace.aiservice.redis.AiCachePort;
import com.enterprise.marketplace.aiservice.repository.AiAuditRepository;
import com.enterprise.marketplace.aiservice.repository.AiChatMessageRepository;
import com.enterprise.marketplace.aiservice.repository.AiChatSessionRepository;
import com.enterprise.marketplace.aiservice.repository.AiGenerationLogRepository;
import com.enterprise.marketplace.aiservice.repository.AiPromptTemplateRepository;
import com.enterprise.marketplace.aiservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.aiservice.service.AiService;
import com.enterprise.marketplace.aiservice.template.AiPromptTemplateEngine;
import com.enterprise.marketplace.common.context.RequestContext;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ObjectProvider<OllamaProvider> ollamaProvider;
    private final AiPromptTemplateRepository promptTemplateRepository;
    private final AiChatSessionRepository chatSessionRepository;
    private final AiChatMessageRepository chatMessageRepository;
    private final AiGenerationLogRepository generationLogRepository;
    private final AiAuditRepository auditRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final AiPromptTemplateEngine templateEngine;
    private final AiCachePort cachePort;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.ollama.default-model:llama3.2}")
    private String defaultModel;

    @Value("${marketplace.ai.feature-enabled:true}")
    private boolean featureEnabledDefault;

    @Value("${marketplace.ai.max-chat-history:20}")
    private int maxChatHistory;

    @Value("${marketplace.outbox.max-retries:5}")
    private int outboxMaxRetries;

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        ensureFeatureEnabled();
        AiPromptTemplateEntity template = loadTemplate(AiUseCase.BUYER_CHAT);
        String sessionKey = resolveSessionKey(request.getSessionKey());
        AiChatSessionEntity session = resolveSession(sessionKey, request.getUserId(), request.getUserRole());

        AiChatMessageEntity userMessage = new AiChatMessageEntity();
        userMessage.setSessionId(session.getId());
        userMessage.setRole(AiChatRole.USER);
        userMessage.setContent(request.getMessage());
        chatMessageRepository.save(userMessage);

        List<OllamaMessage> messages = buildChatMessages(session.getId(), template, request.getMessage());
        OllamaResult result = invokeChat(template.getModel(), messages);
        if (!result.success()) {
            logGeneration(AiUseCase.BUYER_CHAT.name(), template.getModel(), request.getUserId(), null, request.getMessage(),
                    null, AiGenerationStatus.FAILED, result.latencyMs(), result.error());
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "AI chat failed: " + result.error());
        }

        AiChatMessageEntity assistantMessage = new AiChatMessageEntity();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole(AiChatRole.ASSISTANT);
        assistantMessage.setContent(result.content());
        assistantMessage.setModel(result.model());
        chatMessageRepository.save(assistantMessage);

        logGeneration(AiUseCase.BUYER_CHAT.name(), result.model(), request.getUserId(), null, request.getMessage(),
                result.content(), AiGenerationStatus.SUCCESS, result.latencyMs(), null);
        audit(AiAuditOperation.CHAT, request.getUserId(), Map.of("sessionKey", sessionKey));

        saveOutboxEvent(
                session.getId(),
                "ai-chat-session",
                "ai-chat-completed",
                AiKafkaTopics.AI_CHAT_COMPLETED,
                Map.of(
                        "sessionKey", sessionKey,
                        "userId", request.getUserId() != null ? request.getUserId() : "",
                        "reply", result.content(),
                        "model", result.model(),
                        "occurredAt", Instant.now().toString()));

        List<ChatMessageDto> history = chatMessageRepository.findTop20BySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(m -> ChatMessageDto.builder().role(m.getRole().name()).content(m.getContent()).build())
                .toList();

        return ChatResponse.builder()
                .sessionKey(sessionKey)
                .reply(result.content())
                .model(result.model())
                .latencyMs(result.latencyMs())
                .history(history)
                .build();
    }

    @Override
    @Transactional
    public GenerateDescriptionResponse generateDescription(GenerateDescriptionRequest request) {
        ensureFeatureEnabled();
        AiPromptTemplateEntity template = loadTemplate(AiUseCase.PRODUCT_DESCRIPTION);
        Map<String, String> vars = new HashMap<>();
        vars.put("name", request.getName());
        vars.put("sku", request.getSku() != null ? request.getSku() : "");
        vars.put("categoryId", request.getCategoryId() != null ? request.getCategoryId().toString() : "");
        vars.put("attributes", request.getAttributes() != null ? request.getAttributes().toString() : "{}");

        String prompt = templateEngine.render(template.getUserPrompt(), vars);
        OllamaResult result = invokeGenerate(prompt, template.getSystemPrompt(), template.getModel());
        if (!result.success()) {
            logGeneration(AiUseCase.PRODUCT_DESCRIPTION.name(), template.getModel(), null, request.getProductId(),
                    prompt, null, AiGenerationStatus.FAILED, result.latencyMs(), result.error());
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Description generation failed");
        }

        logGeneration(AiUseCase.PRODUCT_DESCRIPTION.name(), result.model(), null, request.getProductId(), prompt,
                result.content(), AiGenerationStatus.SUCCESS, result.latencyMs(), null);
        audit(AiAuditOperation.GENERATE, null, Map.of("productId", request.getProductId().toString()));

        saveOutboxEvent(
                request.getProductId(),
                "product",
                "ai-description-generated",
                AiKafkaTopics.AI_DESCRIPTION_GENERATED,
                Map.of(
                        "productId", request.getProductId(),
                        "description", result.content(),
                        "model", result.model(),
                        "occurredAt", Instant.now().toString()));

        return GenerateDescriptionResponse.builder()
                .productId(request.getProductId())
                .description(result.content())
                .model(result.model())
                .latencyMs(result.latencyMs())
                .build();
    }

    @Override
    @Transactional
    public SearchInterpretResponse interpretSearch(SearchInterpretRequest request) {
        ensureFeatureEnabled();
        AiPromptTemplateEntity template = loadTemplate(AiUseCase.SEARCH_INTERPRET);
        String prompt = templateEngine.render(template.getUserPrompt(), Map.of("query", request.getQuery()));
        OllamaResult result = invokeGenerate(prompt, template.getSystemPrompt(), template.getModel());
        if (!result.success()) {
            throw new MarketplaceException(ErrorCode.INTERNAL_ERROR, "Search interpretation failed");
        }

        audit(AiAuditOperation.INTERPRET, null, Map.of("query", request.getQuery()));
        return parseSearchInterpretation(request.getQuery(), result.content());
    }

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(String buyerId, UUID categoryId, int limit) {
        ensureFeatureEnabled();
        int resolvedLimit = Math.min(Math.max(limit, 1), 10);
        List<RecommendedProductDto> recommendations = new ArrayList<>();

        String prompt = "Suggest " + resolvedLimit + " B2B product types for buyer "
                + (buyerId != null ? buyerId : "anonymous")
                + (categoryId != null ? " in category " + categoryId : "")
                + ". Return one product name per line with a short reason after a dash.";
        OllamaResult result = invokeGenerate(prompt, "You are a B2B marketplace recommendation assistant.", defaultModel);

        if (result.success() && result.content() != null) {
            for (String line : result.content().split("\n")) {
                String trimmed = line.replaceAll("^[-*\\d.\\s]+", "").trim();
                if (trimmed.isBlank()) {
                    continue;
                }
                String name = trimmed;
                String reason = "Recommended by AI";
                int dash = trimmed.indexOf('-');
                if (dash > 0) {
                    name = trimmed.substring(0, dash).trim();
                    reason = trimmed.substring(dash + 1).trim();
                }
                recommendations.add(RecommendedProductDto.builder()
                        .productId(null)
                        .name(name)
                        .reason(reason)
                        .build());
                if (recommendations.size() >= resolvedLimit) {
                    break;
                }
            }
        }

        audit(AiAuditOperation.RECOMMEND, buyerId, Map.of("categoryId", categoryId != null ? categoryId.toString() : ""));
        return RecommendationResponse.builder()
                .buyerId(buyerId)
                .recommendations(recommendations)
                .model(result.model())
                .build();
    }

    @Override
    @Transactional
    public void processKafkaEvent(String payload, String eventSource) {
        audit(AiAuditOperation.EVENT_RECEIVED, null, Map.of("eventSource", eventSource));
        try {
            JsonNode root = objectMapper.readTree(payload);
            if ("admin-feature-toggled".equals(eventSource)) {
                handleFeatureToggle(root);
                return;
            }

            String productId = textOrNull(root, "productId");
            if (productId != null) {
                cachePort.cacheProductSnapshot(productId, payload);
            }

            if ("product-created".equals(eventSource) && isFeatureEnabled()) {
                String description = textOrNull(root, "description");
                String name = textOrNull(root, "name");
                if ((description == null || description.isBlank()) && name != null && productId != null) {
                    GenerateDescriptionRequest req = GenerateDescriptionRequest.builder()
                            .productId(UUID.fromString(productId))
                            .name(name)
                            .sku(textOrNull(root, "sku"))
                            .categoryId(parseUuid(textOrNull(root, "categoryId")))
                            .build();
                    try {
                        generateDescription(req);
                    } catch (Exception ex) {
                        log.warn("Auto description generation failed for product {}", productId, ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to parse kafka payload for {}", eventSource, ex);
        }
    }

    private void handleFeatureToggle(JsonNode root) {
        String feature = textOrNull(root, "feature");
        if (feature == null || !feature.equalsIgnoreCase("ai")) {
            return;
        }
        JsonNode enabledNode = root.get("enabled");
        if (enabledNode != null && enabledNode.isBoolean()) {
            cachePort.setFeatureEnabled(enabledNode.asBoolean());
            audit(AiAuditOperation.FEATURE_TOGGLE, null, Map.of("enabled", enabledNode.asBoolean()));
        }
    }

    private SearchInterpretResponse parseSearchInterpretation(String originalQuery, String raw) {
        try {
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);
            return SearchInterpretResponse.builder()
                    .q(textOrNull(node, "q") != null ? textOrNull(node, "q") : originalQuery)
                    .categoryId(parseUuid(textOrNull(node, "categoryId")))
                    .minPrice(parseDecimal(textOrNull(node, "minPrice")))
                    .maxPrice(parseDecimal(textOrNull(node, "maxPrice")))
                    .status(textOrNull(node, "status"))
                    .rawInterpretation(raw)
                    .build();
        } catch (Exception ex) {
            return SearchInterpretResponse.builder()
                    .q(originalQuery)
                    .rawInterpretation(raw)
                    .build();
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private AiPromptTemplateEntity loadTemplate(AiUseCase useCase) {
        return promptTemplateRepository
                .findFirstByUseCaseAndActiveTrue(useCase)
                .orElseThrow(() -> new MarketplaceException(ErrorCode.RESOURCE_NOT_FOUND, "Prompt template not found"));
    }

    private List<OllamaMessage> buildChatMessages(UUID sessionId, AiPromptTemplateEntity template, String latestUserMessage) {
        List<OllamaMessage> messages = new ArrayList<>();
        if (template.getSystemPrompt() != null && !template.getSystemPrompt().isBlank()) {
            messages.add(new OllamaMessage("system", template.getSystemPrompt()));
        }
        List<AiChatMessageEntity> history =
                chatMessageRepository.findTop20BySessionIdOrderByCreatedAtAsc(sessionId);
        int from = Math.max(0, history.size() - maxChatHistory);
        for (int i = from; i < history.size(); i++) {
            AiChatMessageEntity msg = history.get(i);
            messages.add(new OllamaMessage(msg.getRole().name().toLowerCase(), msg.getContent()));
        }
        if (messages.stream().noneMatch(m -> "user".equals(m.role()) && latestUserMessage.equals(m.content()))) {
            messages.add(new OllamaMessage("user", latestUserMessage));
        }
        return messages;
    }

    private AiChatSessionEntity resolveSession(String sessionKey, String userId, String userRole) {
        return chatSessionRepository.findBySessionKey(sessionKey).orElseGet(() -> {
            AiChatSessionEntity session = new AiChatSessionEntity();
            session.setSessionKey(sessionKey);
            session.setUserId(userId != null ? userId : "anonymous");
            session.setUserRole(userRole != null ? userRole : "BUYER");
            session.setTitle("Marketplace Assistant");
            return chatSessionRepository.save(session);
        });
    }

    private String resolveSessionKey(String sessionKey) {
        return sessionKey != null && !sessionKey.isBlank() ? sessionKey : UUID.randomUUID().toString();
    }

    private OllamaResult invokeChat(String model, List<OllamaMessage> messages) {
        OllamaProvider provider = ollamaProvider.getIfAvailable();
        if (provider == null) {
            return OllamaResult.failure("Ollama provider not available", model != null ? model : defaultModel, 0);
        }
        return provider.chat(messages, model);
    }

    private OllamaResult invokeGenerate(String prompt, String systemPrompt, String model) {
        OllamaProvider provider = ollamaProvider.getIfAvailable();
        if (provider == null) {
            return OllamaResult.failure("Ollama provider not available", model != null ? model : defaultModel, 0);
        }
        return provider.generate(prompt, systemPrompt, model);
    }

    private void ensureFeatureEnabled() {
        if (!isFeatureEnabled()) {
            throw new MarketplaceException(ErrorCode.FORBIDDEN, "AI features are disabled");
        }
    }

    private boolean isFeatureEnabled() {
        return cachePort.getFeatureEnabled().orElse(featureEnabledDefault);
    }

    private void logGeneration(
            String useCase,
            String model,
            String userId,
            UUID aggregateId,
            String prompt,
            String response,
            AiGenerationStatus status,
            long latencyMs,
            String error) {
        AiGenerationLogEntity logEntity = new AiGenerationLogEntity();
        logEntity.setRequestId(RequestContext.getRequestId() != null ? RequestContext.getRequestId() : UUID.randomUUID().toString());
        logEntity.setCorrelationId(RequestContext.getCorrelationId());
        logEntity.setUseCase(useCase);
        logEntity.setModel(model != null ? model : defaultModel);
        logEntity.setUserId(userId);
        logEntity.setAggregateType(aggregateId != null ? "product" : null);
        logEntity.setAggregateId(aggregateId);
        logEntity.setPrompt(prompt);
        logEntity.setResponse(response);
        logEntity.setStatus(status);
        logEntity.setLatencyMs(latencyMs);
        logEntity.setErrorMessage(error);
        generationLogRepository.save(logEntity);
    }

    private void audit(AiAuditOperation operation, String actor, Map<String, Object> metadata) {
        try {
            AiAuditEntity audit = new AiAuditEntity();
            audit.setOperation(operation);
            audit.setActor(actor);
            audit.setCorrelationId(RequestContext.getCorrelationId());
            audit.setRequestId(RequestContext.getRequestId());
            audit.setMetadata(objectMapper.writeValueAsString(metadata));
            auditRepository.save(audit);
        } catch (Exception ex) {
            log.debug("Audit write failed", ex);
        }
    }

    private void saveOutboxEvent(UUID aggregateId, String aggregateType, String eventType, String topic, Map<String, Object> payload) {
        try {
            OutboxEventEntity event = new OutboxEventEntity();
            event.setAggregateId(aggregateId);
            event.setAggregateType(aggregateType);
            event.setEventType(eventType);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setStatus(OutboxEventStatus.PENDING);
            event.setRetryCount(0);
            event.setMaxRetries(outboxMaxRetries);
            event.setCorrelationId(RequestContext.getCorrelationId());
            event.setRequestId(RequestContext.getRequestId());
            outboxEventRepository.save(event);
        } catch (Exception ex) {
            log.warn("Failed to save outbox event type={}", eventType, ex);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
