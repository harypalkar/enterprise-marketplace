package com.enterprise.marketplace.searchservice.repository;

import com.enterprise.marketplace.searchservice.entity.SearchSyncLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchSyncLogRepository extends JpaRepository<SearchSyncLogEntity, UUID> {}
