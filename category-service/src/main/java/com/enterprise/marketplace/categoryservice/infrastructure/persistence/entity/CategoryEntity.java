package com.enterprise.marketplace.categoryservice.infrastructure.persistence.entity;

import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import com.enterprise.marketplace.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CategoryStatus status;
}
