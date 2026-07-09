package com.enterprise.marketplace.inventoryservice.application.mapper;

import com.enterprise.marketplace.inventoryservice.application.dto.CreateInventoryRequest;
import com.enterprise.marketplace.inventoryservice.application.dto.InventoryResponse;
import com.enterprise.marketplace.inventoryservice.domain.model.Inventory;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = InventoryStatus.class)
public interface InventoryMapper {

    InventoryResponse toResponse(Inventory inventory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(
            target = "quantityReserved",
            expression = "java(request.getQuantityReserved() != null ? request.getQuantityReserved() : 0)")
    @Mapping(target = "reorderLevel", expression = "java(request.getReorderLevel() != null ? request.getReorderLevel() : 0)")
    @Mapping(target = "status", expression = "java(request.getStatus() != null ? request.getStatus() : InventoryStatus.IN_STOCK)")
    Inventory toDomain(CreateInventoryRequest request);
}
