package com.enterprise.marketplace.aiservice.repository;

import com.enterprise.marketplace.aiservice.entity.AiChatSessionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiChatSessionRepository extends JpaRepository<AiChatSessionEntity, UUID> {

    Optional<AiChatSessionEntity> findBySessionKey(String sessionKey);
}
