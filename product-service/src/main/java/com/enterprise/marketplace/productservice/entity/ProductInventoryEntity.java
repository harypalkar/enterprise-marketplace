package com.enterprise.marketplace.productservice.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_inventory")
@Getter
@Setter
public class ProductInventoryEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "warehouse_code", length = 32)
    private String warehouseCode;
}
