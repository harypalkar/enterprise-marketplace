package com.enterprise.marketplace.sellerservice.application.service;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.common.util.ValidationUtility;
import com.enterprise.marketplace.sellerservice.application.dto.CreateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.SellerPageResponse;
import com.enterprise.marketplace.sellerservice.application.dto.SellerResponse;
import com.enterprise.marketplace.sellerservice.application.dto.UpdateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.mapper.SellerMapper;
import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerPage;
import com.enterprise.marketplace.sellerservice.domain.model.SellerSearchCriteria;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import com.enterprise.marketplace.sellerservice.domain.port.SellerRepository;
import com.enterprise.marketplace.sellerservice.domain.service.SellerDomainService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerApplicationService {

    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;
    private final ValidationUtility validationUtility;

    @Transactional
    public SellerResponse createSeller(CreateSellerRequest request) {
        String normalizedGstin = normalizeUpper(request.getGstin());
        if (sellerRepository.existsByGstin(normalizedGstin)) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Seller GSTIN already exists: " + normalizedGstin);
        }

        Seller seller = sellerMapper.toDomain(request);
        validateBusinessIdentifiers(seller);
        try {
            SellerDomainService.validateForCreate(seller);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        Seller saved = sellerRepository.save(seller);
        return sellerMapper.toResponse(saved);
    }

    public SellerResponse getSellerById(UUID id) {
        Seller seller = sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        return sellerMapper.toResponse(seller);
    }

    public SellerPageResponse searchSellers(SellerStatus status, String keyword, int page, int size, String sort) {
        SellerSearchCriteria criteria = SellerSearchCriteria.builder()
                .status(status)
                .keyword(StringUtils.hasText(keyword) ? keyword.trim() : null)
                .build();

        SellerPage<Seller> result = sellerRepository.search(criteria, page, size, sort);
        List<SellerResponse> content = result.content().stream().map(sellerMapper::toResponse).toList();

        return SellerPageResponse.builder()
                .content(content)
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .page(result.pageNumber())
                .size(result.pageSize())
                .build();
    }

    @Transactional
    public SellerResponse updateSeller(UUID id, UpdateSellerRequest request) {
        Seller existing =
                sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seller", id));

        if (existing.getStatus() == SellerStatus.ARCHIVED) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, "Archived sellers cannot be updated");
        }

        String normalizedGstin = normalizeUpper(request.getGstin());
        if (!existing.getGstin().equalsIgnoreCase(normalizedGstin)
                && sellerRepository.existsByGstinAndIdNot(normalizedGstin, id)) {
            throw new MarketplaceException(ErrorCode.CONFLICT, "Seller GSTIN already exists: " + normalizedGstin);
        }

        Seller updated = existing.toBuilder()
                .companyName(trimOrExisting(request.getCompanyName(), existing.getCompanyName()))
                .tradeName(trimOrExisting(request.getTradeName(), existing.getTradeName()))
                .gstin(normalizedGstin)
                .pan(normalizeUpper(request.getPan()))
                .email(normalizeEmail(request.getEmail()))
                .phone(trimOrExisting(request.getPhone(), existing.getPhone()))
                .city(trimOrExisting(request.getCity(), existing.getCity()))
                .state(trimOrExisting(request.getState(), existing.getState()))
                .country(trimOrExisting(request.getCountry(), existing.getCountry()))
                .pinCode(trimOrExisting(request.getPinCode(), existing.getPinCode()))
                .build();

        validateBusinessIdentifiers(updated);
        try {
            SellerDomainService.validateForUpdate(updated);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.VALIDATION_ERROR, ex.getMessage());
        }
        return sellerMapper.toResponse(sellerRepository.save(updated));
    }

    @Transactional
    public SellerResponse updateSellerStatus(UUID id, SellerStatus status) {
        Seller existing =
                sellerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Seller", id));

        try {
            SellerDomainService.validateStatusTransition(existing.getStatus(), status);
        } catch (IllegalArgumentException ex) {
            throw new MarketplaceException(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage());
        }

        Seller updated = existing.withStatus(status);
        return sellerMapper.toResponse(sellerRepository.save(updated));
    }

    @Transactional
    public void archiveSeller(UUID id) {
        updateSellerStatus(id, SellerStatus.ARCHIVED);
    }

    private void validateBusinessIdentifiers(Seller seller) {
        validationUtility.requireGstin(seller.getGstin());
        validationUtility.requirePan(seller.getPan());
        validationUtility.requireIndianMobile(seller.getPhone());
        validationUtility.requireEmail(seller.getEmail());
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private String normalizeEmail(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private String trimOrExisting(String candidate, String fallback) {
        return StringUtils.hasText(candidate) ? candidate.trim() : fallback;
    }
}
