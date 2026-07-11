package com.enterprise.marketplace.notificationservice.service;

import com.enterprise.marketplace.notificationservice.entity.NotificationEntity;
import com.enterprise.marketplace.notificationservice.entity.NotificationRetryEntity;
import com.enterprise.marketplace.notificationservice.enums.RetryRecordStatus;
import com.enterprise.marketplace.notificationservice.repository.NotificationRetryRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationRetryService {

    private final NotificationRetryRepository retryRepository;

    @Transactional
    public NotificationRetryEntity scheduleRetry(NotificationEntity notification, String errorMessage) {
        NotificationRetryEntity retry = new NotificationRetryEntity();
        retry.setNotificationId(notification.getId());
        retry.setAttemptNumber(notification.getRetryCount() + 1);
        retry.setStatus(RetryRecordStatus.SCHEDULED);
        retry.setErrorMessage(errorMessage);
        retry.setScheduledAt(Instant.now());
        return retryRepository.save(retry);
    }

    @Transactional
    public void markProcessing(UUID retryId) {
        retryRepository.findById(retryId).ifPresent(retry -> {
            retry.setStatus(RetryRecordStatus.PROCESSING);
            retryRepository.save(retry);
        });
    }

    @Transactional
    public void markCompleted(UUID retryId) {
        retryRepository.findById(retryId).ifPresent(retry -> {
            retry.setStatus(RetryRecordStatus.COMPLETED);
            retry.setProcessedAt(Instant.now());
            retryRepository.save(retry);
        });
    }

    @Transactional
    public void markFailed(UUID retryId, String errorMessage) {
        retryRepository.findById(retryId).ifPresent(retry -> {
            retry.setStatus(RetryRecordStatus.FAILED);
            retry.setErrorMessage(errorMessage);
            retry.setProcessedAt(Instant.now());
            retryRepository.save(retry);
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationRetryEntity> findByNotificationId(UUID notificationId) {
        return retryRepository.findByNotificationIdOrderByAttemptNumberAsc(notificationId);
    }
}
