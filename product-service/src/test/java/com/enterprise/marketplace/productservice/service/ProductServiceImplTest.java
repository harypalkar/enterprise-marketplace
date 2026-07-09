package com.enterprise.marketplace.productservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.productservice.dto.canonical.CanonicalProductRequest;
import com.enterprise.marketplace.productservice.dto.canonical.PricingSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.ProductSectionDto;
import com.enterprise.marketplace.productservice.dto.canonical.RequestHeaderDto;
import com.enterprise.marketplace.productservice.dto.canonical.RequestInfoDto;
import com.enterprise.marketplace.productservice.dto.canonical.SellerSectionDto;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import com.enterprise.marketplace.productservice.mapper.ProductMapper;
import com.enterprise.marketplace.productservice.repository.OutboxEventRepository;
import com.enterprise.marketplace.productservice.repository.ProductAttributeRepository;
import com.enterprise.marketplace.productservice.repository.ProductAuditRepository;
import com.enterprise.marketplace.productservice.repository.ProductDocumentRepository;
import com.enterprise.marketplace.productservice.repository.ProductImageRepository;
import com.enterprise.marketplace.productservice.repository.ProductInventoryRepository;
import com.enterprise.marketplace.productservice.repository.ProductPriceRepository;
import com.enterprise.marketplace.productservice.repository.ProductRepository;
import com.enterprise.marketplace.productservice.repository.ProductWorkflowRepository;
import com.enterprise.marketplace.productservice.service.impl.ProductServiceImpl;
import com.enterprise.marketplace.productservice.validation.ProductValidationPipeline;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductPriceRepository productPriceRepository;
    @Mock
    private ProductInventoryRepository productInventoryRepository;
    @Mock
    private ProductAttributeRepository productAttributeRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductDocumentRepository productDocumentRepository;
    @Mock
    private ProductWorkflowRepository productWorkflowRepository;
    @Mock
    private ProductAuditRepository productAuditRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private ProductValidationPipeline validationPipeline;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldInvokeValidationPipelineOnCreate() {
        CanonicalProductRequest request = minimalRequest("SKU-NEW");

        try {
            productService.createProduct(request);
        } catch (Exception ignored) {
            // partial mocks may fail after validation; pipeline invocation is the assertion target
        }

        verify(validationPipeline).validate(any());
    }

    private CanonicalProductRequest minimalRequest(String sku) {
        return CanonicalProductRequest.builder()
                .header(RequestHeaderDto.builder()
                        .sourceSystem("test")
                        .channel("API")
                        .locale("en-IN")
                        .build())
                .requestInfo(RequestInfoDto.builder()
                        .clientRequestId(UUID.randomUUID().toString())
                        .idempotencyKey(UUID.randomUUID().toString())
                        .requestedBy("tester")
                        .build())
                .seller(SellerSectionDto.builder().sellerId(UUID.randomUUID()).build())
                .product(ProductSectionDto.builder()
                        .sku(sku)
                        .name("Test Product")
                        .unitOfMeasure("PCS")
                        .status(ProductStatus.DRAFT)
                        .build())
                .pricing(PricingSectionDto.builder()
                        .unitPrice(BigDecimal.TEN)
                        .currency("INR")
                        .minQuantity(1)
                        .validFrom(Instant.now())
                        .build())
                .build();
    }
}
