package com.enterprise.marketplace.notificationservice.repository;

import com.enterprise.marketplace.notificationservice.entity.NotificationChannelEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationChannel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannelEntity, UUID> {

    Optional<NotificationChannelEntity> findByChannel(NotificationChannel channel);

    Optional<NotificationChannelEntity> findByChannelAndEnabledTrue(NotificationChannel channel);
}
