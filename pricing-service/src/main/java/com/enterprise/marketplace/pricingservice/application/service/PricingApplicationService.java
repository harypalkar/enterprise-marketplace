package com.enterprise.marketplace.pricingservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.pricingservice.application.dto.CreatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.PricingPageResponse;
import com.enterprise.marketplace.pricingservice.application.dto.PricingResponse;
import com.enterprise.marketplace.pricingservice.application.dto.UpdatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.mapper.PricingMapper;
import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingPage;
import com.enterprise.marketplace.pricingservice.domain.model.PricingSearchCriteria;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import com.enterprise.marketplace.pricingservice.domain.port.PricingRepository;
import com.enterprise.marketplace.pricingservice.domain.service.PricingDomainService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingApplicationService {

    private final PricingRepository pricingRepository;
    private final PricingMapper pricingMapper;

    @Transactional
    public PricingResponse createPricing(CreatePricingRequest request) {
        Pricing pricing = pricingMapper.toDomain(request);
        try {
            PricingDomainService.validateForCreate(pricing);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        Pricing saved = pricingRepository.save(pricing);
        return pricingMapper.toResponse(saved);
    }

    public PricingResponse getPricingById(UUID id) {
        Pricing pricing = pricingRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing", id));
        return pricingMapper.toResponse(pricing);
    }

    public PricingPageResponse searchPricing(
            UUID productId, UUID sellerId, PricingStatus status, int page, int size, String sort) {
        PricingSearchCriteria criteria = PricingSearchCriteria.builder()
                .productId(productId)
                .sellerId(sellerId)
                .status(status)
                .build();
        PricingPage<Pricing> result = pricingRepository.search(criteria, page, size, sort);
        List<PricingResponse> content =
                result.content().stream().map(pricingMapper::toResponse).toList();

        return PricingPageResponse.builder()
                .content(content)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .page(result.pageNumber())
                .size(result.pageSize())
                .build();
    }

    @Transactional
    public PricingResponse updatePricing(UUID id, UpdatePricingRequest request) {
        Pricing existing = pricingRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing", id));

        Pricing updated = existing.toBuilder()
                .unitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : existing.getUnitPrice())
                .currency(StringUtils.hasText(request.getCurrency())
                        ? request.getCurrency().toUpperCase(Locale.ROOT)
                        : existing.getCurrency())
                .minQuantity(request.getMinQuantity() != null ? request.getMinQuantity() : existing.getMinQuantity())
                .discountPercent(request.getDiscountPercent() != null
                        ? request.getDiscountPercent()
                        : existing.getDiscountPercent())
                .validFrom(request.getValidFrom() != null ? request.getValidFrom() : existing.getValidFrom())
                .validTo(request.getValidTo() != null ? request.getValidTo() : existing.getValidTo())
                .build();

        try {
            PricingDomainService.validateForUpdate(updated);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        return pricingMapper.toResponse(pricingRepository.save(updated));
    }

    @Transactional
    public PricingResponse updatePricingStatus(UUID id, PricingStatus status) {
        Pricing existing = pricingRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing", id));
        try {
            PricingDomainService.validateStatusTransition(existing.getStatus(), status);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage());
        }
        Pricing updated = existing.withStatus(status);
        return pricingMapper.toResponse(pricingRepository.save(updated));
    }

    @Transactional
    public void deletePricing(UUID id) {
        pricingRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing", id));
        pricingRepository.deleteById(id);
    }
}
