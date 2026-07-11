package com.enterprise.marketplace.notificationservice.repository;

import com.enterprise.marketplace.notificationservice.entity.NotificationInboxEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationInboxRepository extends JpaRepository<NotificationInboxEntity, UUID> {

    Page<NotificationInboxEntity> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);
}
