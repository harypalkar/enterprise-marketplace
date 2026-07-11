package com.enterprise.marketplace.searchservice.repository;

import com.enterprise.marketplace.searchservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.searchservice.enums.OutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
