package com.enterprise.marketplace.buyerservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.buyerservice.domain.model.BuyerStatus;
import com.enterprise.marketplace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "buyer")
@Getter
@Setter
public class BuyerEntity extends BaseEntity {

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "contact_person", nullable = false, length = 128)
    private String contactPerson;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 32)
    private String phone;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "pin_code", nullable = false, length = 12)
    private String pinCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BuyerStatus status;
}
