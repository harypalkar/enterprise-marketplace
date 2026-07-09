package com.enterprise.marketplace.pricingservice.infrastructure.persistence.mapper;

import com.enterprise.marketplace.pricingservice.domain.model.Pricing;
import com.enterprise.marketplace.pricingservice.infrastructure.persistence.entity.PricingEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PricingPersistenceMapper {

    Pricing toDomain(PricingEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    PricingEntity toEntity(Pricing pricing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget PricingEntity entity, Pricing pricing);
}
