package com.enterprise.marketplace.sellerservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import com.enterprise.marketplace.common.util.ValidationUtility;
import com.enterprise.marketplace.sellerservice.application.dto.CreateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.UpdateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.mapper.SellerMapper;
import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import com.enterprise.marketplace.sellerservice.domain.port.SellerRepository;
import jakarta.validation.Validation;
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
class SellerApplicationServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Spy
    private SellerMapper sellerMapper = Mappers.getMapper(SellerMapper.class);

    @Spy
    private ValidationUtility validationUtility =
            new ValidationUtility(Validation.buildDefaultValidatorFactory().getValidator());

    @InjectMocks
    private SellerApplicationService sellerApplicationService;

    @Test
    void shouldCreateSellerWhenGstinIsUnique() {
        CreateSellerRequest request = CreateSellerRequest.builder()
                .companyName("Acme Components Pvt Ltd")
                .tradeName("Acme Components")
                .gstin("27AABCU9603R1ZM")
                .pan("AABCU9603R")
                .email("sales@acme.in")
                .phone("9876543210")
                .city("Pune")
                .state("Maharashtra")
                .pinCode("411001")
                .build();

        when(sellerRepository.existsByGstin("27AABCU9603R1ZM")).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenAnswer(invocation -> {
            Seller seller = invocation.getArgument(0);
            return seller.toBuilder().id(UUID.randomUUID()).version(0L).build();
        });

        var response = sellerApplicationService.createSeller(request);

        assertThat(response.getGstin()).isEqualTo("27AABCU9603R1ZM");
        assertThat(response.getStatus()).isEqualTo(SellerStatus.PENDING);
        verify(sellerRepository).save(any(Seller.class));
    }

    @Test
    void shouldThrowConflictWhenGstinAlreadyExists() {
        CreateSellerRequest request = CreateSellerRequest.builder()
                .companyName("Duplicate Seller")
                .tradeName("Duplicate")
                .gstin("29AAECS1234F1Z5")
                .pan("AAECS1234F")
                .email("dup@example.com")
                .phone("9876543210")
                .city("Bengaluru")
                .state("Karnataka")
                .pinCode("560001")
                .build();

        when(sellerRepository.existsByGstin("29AAECS1234F1Z5")).thenReturn(true);

        assertThatThrownBy(() -> sellerApplicationService.createSeller(request))
                .isInstanceOf(MarketplaceException.class)
                .extracting(ex -> ((MarketplaceException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void shouldThrowNotFoundWhenSellerDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(sellerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerApplicationService.getSellerById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateSellerWhenNotArchived() {
        UUID id = UUID.randomUUID();
        Seller existing = Seller.builder()
                .id(id)
                .companyName("Old Company")
                .tradeName("Old Trade")
                .gstin("27AABCU9603R1ZM")
                .pan("AABCU9603R")
                .email("old@acme.in")
                .phone("9876543210")
                .city("Pune")
                .state("Maharashtra")
                .country("India")
                .pinCode("411001")
                .status(SellerStatus.ACTIVE)
                .version(1L)
                .build();

        when(sellerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sellerRepository.save(any(Seller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSellerRequest request = UpdateSellerRequest.builder()
                .companyName("Updated Company")
                .tradeName("Updated Trade")
                .gstin("27AABCU9603R1ZM")
                .pan("AABCU9603R")
                .email("new@acme.in")
                .phone("9876543211")
                .city("Mumbai")
                .state("Maharashtra")
                .country("India")
                .pinCode("400001")
                .build();

        var response = sellerApplicationService.updateSeller(id, request);

        assertThat(response.getCompanyName()).isEqualTo("Updated Company");
        assertThat(response.getPhone()).isEqualTo("9876543211");
    }
}
