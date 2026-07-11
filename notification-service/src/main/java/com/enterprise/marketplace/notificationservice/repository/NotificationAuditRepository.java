package com.enterprise.marketplace.notificationservice.repository;

import com.enterprise.marketplace.notificationservice.entity.NotificationAuditEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAuditRepository extends JpaRepository<NotificationAuditEntity, UUID> {

    List<NotificationAuditEntity> findByNotificationIdOrderByCreatedAtDesc(UUID notificationId);
}
