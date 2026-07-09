package com.enterprise.marketplace.pricingservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.pricingservice.domain.model.PricingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pricing")
@Getter
@Setter
public class PricingEntity extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PricingStatus status;
}
