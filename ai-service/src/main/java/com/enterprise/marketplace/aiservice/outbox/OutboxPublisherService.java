package com.enterprise.marketplace.aiservice.outbox;

import com.enterprise.marketplace.aiservice.entity.OutboxEventEntity;
import com.enterprise.marketplace.aiservice.enums.OutboxEventStatus;
import com.enterprise.marketplace.aiservice.kafka.AiKafkaEventPublisher;
import com.enterprise.marketplace.aiservice.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "marketplace.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(AiKafkaEventPublisher.class)
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final AiKafkaEventPublisher kafkaEventPublisher;

    @Value("${marketplace.outbox.max-retries:5}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${marketplace.outbox.publish-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> pending =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);
        for (OutboxEventEntity event : pending) {
            publishSingle(event);
        }
    }

    private void publishSingle(OutboxEventEntity event) {
        try {
            Map<String, String> headers = new HashMap<>();
            if (event.getCorrelationId() != null) {
                headers.put("correlationId", event.getCorrelationId());
            }
            if (event.getRequestId() != null) {
                headers.put("requestId", event.getRequestId());
            }
            headers.put("eventType", event.getEventType());
            kafkaEventPublisher.publish(
                    event.getTopic(), event.getAggregateId().toString(), event.getPayload(), headers);
            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(ex.getMessage());
            event.setStatus(event.getRetryCount() >= event.getMaxRetries()
                    ? OutboxEventStatus.DEAD_LETTER
                    : OutboxEventStatus.FAILED);
            log.warn("Outbox publish failed id={} retry={}", event.getId(), event.getRetryCount(), ex);
        }
        event.setUpdatedAt(Instant.now());
        outboxEventRepository.save(event);
    }
}
