package com.enterprise.marketplace.productservice.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.productservice.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
public class ProductEntity extends BaseEntity {

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "min_order_quantity", nullable = false)
    private Integer minOrderQuantity;

    @Column(name = "unit_of_measure", nullable = false, length = 32)
    private String unitOfMeasure;

    @Column(name = "hsn_code", length = 16)
    private String hsnCode;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ProductStatus status;
}
