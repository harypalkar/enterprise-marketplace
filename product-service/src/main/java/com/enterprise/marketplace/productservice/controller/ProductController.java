package com.enterprise.marketplace.productservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductPatchRequest;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.ProductDetailResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductPageResponse;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSearchRequest;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.enterprise.marketplace.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Enterprise product catalog APIs with canonical request envelope")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Idempotent
    @Operation(
            summary = "Create product",
            description = "Accepts enterprise canonical request envelope. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(
            @Valid @RequestBody CanonicalProductRequest request) {
        ProductDetailResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(id)));
    }

    @GetMapping
    @Operation(summary = "List products with pagination")
    public ResponseEntity<ApiResponse<ProductPageResponse>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(productService.listProducts(page, size, sort)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<ApiResponse<ProductPageResponse>> searchProducts(
            @Valid @ModelAttribute ProductSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(request)));
    }

    @PutMapping("/{id}")
    @Idempotent
    @Operation(summary = "Update product (full canonical envelope)")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody CanonicalProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.updateProduct(id, request), "Product updated successfully"));
    }

    @PatchMapping("/{id}")
    @Idempotent
    @Operation(summary = "Partial update product")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> patchProduct(
            @PathVariable UUID id, @Valid @RequestBody CanonicalProductPatchRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.patchProduct(id, request), "Product patched successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (archive) product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product archived successfully"));
    }
}
