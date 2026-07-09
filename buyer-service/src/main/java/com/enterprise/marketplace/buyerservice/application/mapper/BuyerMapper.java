package com.enterprise.marketplace.buyerservice.application.mapper;

import com.enterprise.marketplace.buyerservice.application.dto.BuyerResponse;
import com.enterprise.marketplace.buyerservice.application.dto.CreateBuyerRequest;
import com.enterprise.marketplace.buyerservice.domain.model.Buyer;
import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = BuyerStatus.class)
public interface BuyerMapper {

    BuyerResponse toResponse(Buyer buyer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : BuyerStatus.ACTIVE)")
    @Mapping(target = "country", expression = "java(normalizeCountry(request.getCountry()))")
    @Mapping(target = "email", expression = "java(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)")
    @Mapping(target = "pinCode", expression = "java(request.getPinCode() != null ? request.getPinCode().trim().toUpperCase() : null)")
    Buyer toDomain(CreateBuyerRequest request);

    default String normalizeCountry(String country) {
        return country != null && !country.isBlank() ? country.trim() : "India";
    }
}
