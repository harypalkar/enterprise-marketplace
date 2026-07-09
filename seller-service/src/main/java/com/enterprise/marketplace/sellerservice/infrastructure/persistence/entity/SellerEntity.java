package com.enterprise.marketplace.sellerservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import com.enterprise.marketplace.sellerservice.domain.model.SellerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "seller")
@Getter
@Setter
public class SellerEntity extends BaseEntity {

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "trade_name", nullable = false, length = 255)
    private String tradeName;

    @Column(name = "gstin", nullable = false, unique = true, length = 15)
    private String gstin;

    @Column(name = "pan", nullable = false, length = 10)
    private String pan;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 10)
    private String phone;

    @Column(name = "city", nullable = false, length = 128)
    private String city;

    @Column(name = "state", nullable = false, length = 128)
    private String state;

    @Column(name = "country", nullable = false, length = 128)
    private String country;

    @Column(name = "pin_code", nullable = false, length = 10)
    private String pinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SellerStatus status;
}
