package com.enterprise.marketplace.buyerservice.application.service;

import com.enterprise.marketplace.buyerservice.application.dto.BuyerPageResponse;
import com.enterprise.marketplace.buyerservice.application.dto.BuyerResponse;
import com.enterprise.marketplace.buyerservice.application.dto.CreateBuyerRequest;
import com.enterprise.marketplace.buyerservice.application.dto.UpdateBuyerRequest;
import com.enterprise.marketplace.buyerservice.application.mapper.BuyerMapper;
import com.enterprise.marketplace.buyerservice.domain.model.Buyer;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerPage;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerSearchCriteria;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import com.enterprise.marketplace.buyerservice.domain.port.BuyerRepository;
import com.enterprise.marketplace.buyerservice.domain.service.BuyerDomainService;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerApplicationService {

    private static final String DEFAULT_COUNTRY = "India";
    private final BuyerRepository buyerRepository;
    private final BuyerMapper buyerMapper;

    @Transactional
    public BuyerResponse createBuyer(CreateBuyerRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (buyerRepository.existsByEmail(normalizedEmail)) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Buyer email already exists: " + normalizedEmail);
        }

        Buyer buyer = buyerMapper.toDomain(request).withEmail(normalizedEmail);
        try {
            BuyerDomainService.validateForCreate(buyer);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        Buyer saved = buyerRepository.save(buyer);
        return buyerMapper.toResponse(saved);
    }

    public BuyerResponse getBuyerById(UUID id) {
        Buyer buyer = buyerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        return buyerMapper.toResponse(buyer);
    }

    public BuyerPageResponse searchBuyers(
            BuyerStatus status, String city, String state, String country, String keyword, int page, int size, String sort) {
        BuyerSearchCriteria criteria = BuyerSearchCriteria.builder()
                .status(status)
                .city(normalizeNullable(city))
                .state(normalizeNullable(state))
                .country(normalizeNullable(country))
                .keyword(StringUtils.hasText(keyword) ? keyword.trim() : null)
                .build();

        BuyerPage<Buyer> result = buyerRepository.search(criteria, page, size, sort);
        List<BuyerResponse> content = result.content().stream().map(buyerMapper::toResponse).toList();

        return BuyerPageResponse.builder()
                .content(content)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .page(result.pageNumber())
                .size(result.pageSize())
                .build();
    }

    @Transactional
    public BuyerResponse updateBuyer(UUID id, UpdateBuyerRequest request) {
        Buyer existing = buyerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        if (existing.getStatus() == BuyerStatus.ARCHIVED) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Archived buyers cannot be updated");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        if (buyerRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Buyer email already exists: " + normalizedEmail);
        }

        Buyer updated = existing.toBuilder()
                .companyName(request.getCompanyName().trim())
                .contactPerson(request.getContactPerson().trim())
                .email(normalizedEmail)
                .phone(request.getPhone().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .country(normalizeCountry(request.getCountry()))
                .pinCode(request.getPinCode().trim().toUpperCase())
                .build();

        try {
            BuyerDomainService.validateForUpdate(updated);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        return buyerMapper.toResponse(buyerRepository.save(updated));
    }

    @Transactional
    public BuyerResponse updateBuyerStatus(UUID id, BuyerStatus status) {
        Buyer existing = buyerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        try {
            BuyerDomainService.validateStatusTransition(existing.getStatus(), status);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage());
        }

        Buyer updated = existing.withStatus(status);
        return buyerMapper.toResponse(buyerRepository.save(updated));
    }

    @Transactional
    public void archiveBuyer(UUID id) {
        updateBuyerStatus(id, BuyerStatus.ARCHIVED);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizeCountry(String country) {
        if (!StringUtils.hasText(country)) {
            return DEFAULT_COUNTRY;
        }
        return country.trim();
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
