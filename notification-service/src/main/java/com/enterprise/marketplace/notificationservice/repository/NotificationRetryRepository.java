package com.enterprise.marketplace.notificationservice.repository;

import com.enterprise.marketplace.notificationservice.entity.NotificationRetryEntity;
import com.enterprise.marketplace.notificationservice.enums.RetryRecordStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRetryRepository extends JpaRepository<NotificationRetryEntity, UUID> {

    List<NotificationRetryEntity> findByNotificationIdOrderByAttemptNumberAsc(UUID notificationId);

    List<NotificationRetryEntity> findTop50ByStatusOrderByScheduledAtAsc(RetryRecordStatus status);
}
