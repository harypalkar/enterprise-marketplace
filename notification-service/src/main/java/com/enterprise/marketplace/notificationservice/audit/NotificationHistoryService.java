package com.enterprise.marketplace.notificationservice.audit;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationHistoryEntity;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.repository.NotificationHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationHistoryService {

    private final NotificationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void recordHistory(
            NotificationEntity notification, String eventType, NotificationStatus status, String message) {
        recordHistory(notification, eventType, status, message, null);
    }

    @Transactional
    public void recordHistory(
            NotificationEntity notification,
            String eventType,
            NotificationStatus status,
            String message,
            Map<String, Object> metadata) {
        NotificationHistoryEntity history = new NotificationHistoryEntity();
        history.setNotificationId(notification.getId());
        history.setStatus(status);
        history.setChannel(notification.getChannel());
        history.setEventType(eventType);
        history.setMessage(message);
        if (metadata != null && !metadata.isEmpty()) {
            try {
                history.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception ignored) {
                history.setMetadata("{}");
            }
        }
        historyRepository.save(history);
    }
}
