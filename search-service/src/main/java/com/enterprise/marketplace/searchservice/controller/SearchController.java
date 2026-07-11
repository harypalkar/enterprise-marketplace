package com.enterprise.marketplace.searchservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.searchservice.dto.ProductSearchPageResponse;
import com.enterprise.marketplace.searchservice.dto.ProductSearchRequest;
import com.enterprise.marketplace.searchservice.dto.ProductSearchResult;
import com.enterprise.marketplace.searchservice.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Elasticsearch product search APIs")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/products")
    @Operation(summary = "Search products with full-text and filters")
    public ResponseEntity<ApiResponse<ProductSearchPageResponse>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "indexedAt,desc") String sort) {
        ProductSearchRequest request = ProductSearchRequest.builder()
                .q(q)
                .sellerId(sellerId)
                .categoryId(categoryId)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .page(page)
                .size(size)
                .sort(sort)
                .build();
        return ResponseEntity.ok(ApiResponse.success(searchService.searchProducts(request)));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get indexed product by ID")
    public ResponseEntity<ApiResponse<ProductSearchResult>> getProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(searchService.getProductById(productId)));
    }

    @PostMapping("/products/{productId}/reindex")
    @Operation(summary = "Reindex a product document in Elasticsearch")
    public ResponseEntity<ApiResponse<ProductSearchResult>> reindexProduct(
            @PathVariable UUID productId, @Valid @RequestBody Map<String, Object> payload) {
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            return ResponseEntity.ok(ApiResponse.success(searchService.reindexProduct(productId, json)));
        } catch (Exception ex) {
            throw new com.enterprise.marketplace.common.exception.MarketplaceException(
                    com.enterprise.marketplace.common.exception.ErrorCode.VALIDATION_ERROR, "Invalid reindex payload");
        }
    }
}
