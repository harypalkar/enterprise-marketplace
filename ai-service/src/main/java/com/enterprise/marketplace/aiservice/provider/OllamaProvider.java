package com.enterprise.marketplace.aiservice.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.ollama", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OllamaProvider {

    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper;

    @Value("${marketplace.ollama.default-model:llama3.2}")
    private String defaultModel;

    @Value("${marketplace.ollama.timeout-seconds:60}")
    private long timeoutSeconds;

    public OllamaResult chat(List<OllamaMessage> messages, String model) {
        String resolvedModel = model != null && !model.isBlank() ? model : defaultModel;
        Map<String, Object> body = new HashMap<>();
        body.put("model", resolvedModel);
        body.put("stream", false);
        body.put("messages", toPayloadMessages(messages));

        long start = System.currentTimeMillis();
        try {
            String responseBody = ollamaWebClient
                    .post()
                    .uri("/api/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("message").path("content").asText("");
            return OllamaResult.success(content, resolvedModel, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.warn("Ollama chat failed model={}", resolvedModel, ex);
            return OllamaResult.failure(ex.getMessage(), resolvedModel, System.currentTimeMillis() - start);
        }
    }

    public OllamaResult generate(String prompt, String systemPrompt, String model) {
        String resolvedModel = model != null && !model.isBlank() ? model : defaultModel;
        Map<String, Object> body = new HashMap<>();
        body.put("model", resolvedModel);
        body.put("stream", false);
        body.put("prompt", prompt);
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("system", systemPrompt);
        }

        long start = System.currentTimeMillis();
        try {
            String responseBody = ollamaWebClient
                    .post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(timeoutSeconds));
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("response").asText("");
            return OllamaResult.success(content, resolvedModel, System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.warn("Ollama generate failed model={}", resolvedModel, ex);
            return OllamaResult.failure(ex.getMessage(), resolvedModel, System.currentTimeMillis() - start);
        }
    }

    private List<Map<String, String>> toPayloadMessages(List<OllamaMessage> messages) {
        List<Map<String, String>> payload = new ArrayList<>();
        for (OllamaMessage message : messages) {
            Map<String, String> entry = new HashMap<>();
            entry.put("role", message.role());
            entry.put("content", message.content());
            payload.add(entry);
        }
        return payload;
    }

    public record OllamaMessage(String role, String content) {}

    public record OllamaResult(boolean success, String content, String model, long latencyMs, String error) {

        static OllamaResult success(String content, String model, long latencyMs) {
            return new OllamaResult(true, content, model, latencyMs, null);
        }

        public static OllamaResult failure(String error, String model, long latencyMs) {
            return new OllamaResult(false, null, model, latencyMs, error);
        }
    }
}
