package com.enterprise.marketplace.notificationservice.repository;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByIdAndActiveTrue(UUID id);

    Optional<NotificationEntity> findByRequestIdAndActiveTrue(String requestId);

    Page<NotificationEntity> findByRecipientIdAndActiveTrue(String recipientId, Pageable pageable);

    Page<NotificationEntity> findByStatusAndActiveTrue(NotificationStatus status, Pageable pageable);

    List<NotificationEntity> findTop50ByStatusInAndActiveTrueOrderByCreatedAtAsc(
            Collection<NotificationStatus> statuses);
}
