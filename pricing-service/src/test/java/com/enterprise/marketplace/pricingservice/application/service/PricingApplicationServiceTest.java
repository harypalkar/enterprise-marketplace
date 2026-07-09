package com.enterprise.marketplace.pricingservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.pricingservice.application.dto.CreatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.UpdatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.mapper.PricingMapper;
import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import com.enterprise.marketplace.pricingservice.domain.port.PricingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingApplicationServiceTest {

    @Mock
    private PricingRepository pricingRepository;

    @Spy
    private PricingMapper pricingMapper = Mappers.getMapper(PricingMapper.class);

    @InjectMocks
    private PricingApplicationService pricingApplicationService;

    @Test
    void shouldCreatePricingWhenRequestIsValid() {
        CreatePricingRequest request = CreatePricingRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .unitPrice(new BigDecimal("1500.00"))
                .currency("INR")
                .validFrom(Instant.parse("2026-07-01T00:00:00Z"))
                .build();

        when(pricingRepository.save(any(Pricing.class))).thenAnswer(invocation -> {
            Pricing pricing = invocation.getArgument(0);
            return pricing.toBuilder().id(UUID.randomUUID()).version(0L).build();
        });

        var response = pricingApplicationService.createPricing(request);

        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getStatus()).isEqualTo(PricingStatus.ACTIVE);
        verify(pricingRepository).save(any(Pricing.class));
    }

    @Test
    void shouldThrowValidationErrorForInvalidDiscountPercent() {
        CreatePricingRequest request = CreatePricingRequest.builder()
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .unitPrice(BigDecimal.TEN)
                .currency("INR")
                .discountPercent(new BigDecimal("101"))
                .validFrom(Instant.parse("2026-07-01T00:00:00Z"))
                .build();

        assertThatThrownBy(() -> pricingApplicationService.createPricing(request))
                .isInstanceOf(MarketplaceException.class)
                .extracting(ex -> ((MarketplaceException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldThrowNotFoundWhenPricingDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(pricingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingApplicationService.getPricingById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdatePricingWhenEntryExists() {
        UUID id = UUID.randomUUID();
        Pricing existing = Pricing.builder()
                .id(id)
                .productId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .unitPrice(BigDecimal.TEN)
                .currency("INR")
                .minQuantity(1)
                .validFrom(Instant.parse("2026-07-01T00:00:00Z"))
                .status(PricingStatus.ACTIVE)
                .version(1L)
                .build();

        when(pricingRepository.findById(id)).thenReturn(Optional.of(existing));
        when(pricingRepository.save(any(Pricing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePricingRequest request = UpdatePricingRequest.builder()
                .unitPrice(new BigDecimal("99.99"))
                .discountPercent(new BigDecimal("5.00"))
                .build();

        var response = pricingApplicationService.updatePricing(id, request);

        assertThat(response.getUnitPrice()).isEqualByComparingTo("99.99");
        assertThat(response.getDiscountPercent()).isEqualByComparingTo("5.00");
    }
}
