package com.enterprise.marketplace.aiservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.aiservice.dto.ChatRequest;
import com.enterprise.marketplace.aiservice.dto.ChatResponse;
import com.enterprise.marketplace.aiservice.dto.GenerateDescriptionRequest;
import com.enterprise.marketplace.aiservice.dto.GenerateDescriptionResponse;
import com.enterprise.marketplace.aiservice.dto.RecommendationResponse;
import com.enterprise.marketplace.aiservice.dto.SearchInterpretRequest;
import com.enterprise.marketplace.aiservice.dto.SearchInterpretResponse;
import com.enterprise.marketplace.aiservice.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "Ollama-powered marketplace AI APIs")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Buyer/seller chat assistant")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.chat(request)));
    }

    @PostMapping("/generate/description")
    @Operation(summary = "Generate product description using AI")
    public ResponseEntity<ApiResponse<GenerateDescriptionResponse>> generateDescription(
            @Valid @RequestBody GenerateDescriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.generateDescription(request)));
    }

    @PostMapping("/search/interpret")
    @Operation(summary = "Interpret natural language search query into structured filters")
    public ResponseEntity<ApiResponse<SearchInterpretResponse>> interpretSearch(
            @Valid @RequestBody SearchInterpretRequest request) {
        return ResponseEntity.ok(ApiResponse.success(aiService.interpretSearch(request)));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get AI product recommendations for a buyer")
    public ResponseEntity<ApiResponse<RecommendationResponse>> recommendations(
            @RequestParam(required = false) String buyerId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(aiService.getRecommendations(buyerId, categoryId, limit)));
    }
}
