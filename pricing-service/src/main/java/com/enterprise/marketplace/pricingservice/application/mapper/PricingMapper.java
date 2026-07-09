package com.enterprise.marketplace.pricingservice.application.mapper;

import com.enterprise.marketplace.pricingservice.application.dto.CreatePricingRequest;
import com.enterprise.marketplace.pricingservice.application.dto.PricingResponse;
import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = PricingStatus.class)
public interface PricingMapper {

    PricingResponse toResponse(Pricing pricing);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(
            target = "status",
            expression = "java(request.getStatus() != null ? request.getStatus() : PricingStatus.ACTIVE)")
    @Mapping(
            target = "minQuantity",
            expression = "java(request.getMinQuantity() != null ? request.getMinQuantity() : 1)")
    @Mapping(
            target = "currency",
            expression = "java(request.getCurrency() != null ? request.getCurrency().toUpperCase() : \"INR\")")
    Pricing toDomain(CreatePricingRequest request);
}
