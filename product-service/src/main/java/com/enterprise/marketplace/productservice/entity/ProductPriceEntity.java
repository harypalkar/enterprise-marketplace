package com.enterprise.marketplace.productservice.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_price")
@Getter
@Setter
public class ProductPriceEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;
}
