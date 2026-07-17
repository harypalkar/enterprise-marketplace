package com.enterprise.marketplace.identityservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
public class UserProfileEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "legal_name", length = 255)
    private String legalName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "website", length = 512)
    private String website;

    @Column(name = "gst_number", length = 32)
    private String gstNumber;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;
}
