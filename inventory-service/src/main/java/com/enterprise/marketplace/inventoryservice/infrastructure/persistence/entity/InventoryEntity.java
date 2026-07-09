package com.enterprise.marketplace.inventoryservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.inventoryservice.domain.model.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
public class InventoryEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "warehouse_code", length = 32)
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InventoryStatus status;
}
