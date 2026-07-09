package com.enterprise.marketplace.sellerservice.application.mapper;

import com.enterprise.marketplace.sellerservice.application.dto.CreateSellerRequest;
import com.enterprise.marketplace.sellerservice.application.dto.SellerResponse;
import com.enterprise.marketplace.sellerservice.domain.model.Seller;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = SellerStatus.class)
public interface SellerMapper {

    SellerResponse toResponse(Seller seller);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : SellerStatus.PENDING)")
    @Mapping(target = "country", expression = "java(request.getCountry() != null ? request.getCountry().trim() : \"India\")")
    @Mapping(target = "gstin", expression = "java(request.getGstin() != null ? request.getGstin().trim().toUpperCase() : null)")
    @Mapping(target = "pan", expression = "java(request.getPan() != null ? request.getPan().trim().toUpperCase() : null)")
    @Mapping(target = "email", expression = "java(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null)")
    @Mapping(target = "phone", expression = "java(request.getPhone() != null ? request.getPhone().trim() : null)")
    @Mapping(target = "companyName", expression = "java(request.getCompanyName() != null ? request.getCompanyName().trim() : null)")
    @Mapping(target = "tradeName", expression = "java(request.getTradeName() != null ? request.getTradeName().trim() : null)")
    @Mapping(target = "city", expression = "java(request.getCity() != null ? request.getCity().trim() : null)")
    @Mapping(target = "state", expression = "java(request.getState() != null ? request.getState().trim() : null)")
    @Mapping(target = "pinCode", expression = "java(request.getPinCode() != null ? request.getPinCode().trim() : null)")
    Seller toDomain(CreateSellerRequest request);
}
