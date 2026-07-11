package com.enterprise.marketplace.searchservice.repository;

import com.enterprise.marketplace.searchservice.entity.SearchAuditEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchAuditRepository extends JpaRepository<SearchAuditEntity, UUID> {}
