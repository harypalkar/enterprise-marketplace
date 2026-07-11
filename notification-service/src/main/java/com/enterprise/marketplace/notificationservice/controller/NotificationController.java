package com.enterprise.marketplace.notificationservice.controller;

import com.enterprise.marketplace.common.api.ApiResponse;
import com.enterprise.marketplace.common.constant.HttpHeaders;
import com.enterprise.marketplace.common.idempotency.Idempotent;
import com.enterprise.marketplace.notificationservice.dto.CreateNotificationRequest;
import com.enterprise.marketplace.notificationservice.dto.NotificationPageResponse;
import com.enterprise.marketplace.notificationservice.dto.NotificationResponse;
import com.enterprise.marketplace.notificationservice.dto.RetryNotificationRequest;
import com.enterprise.marketplace.notificationservice.dto.RetryNotificationResponse;
import com.enterprise.marketplace.notificationservice.dto.SendNotificationRequest;
import com.enterprise.marketplace.notificationservice.dto.StatusUpdateRequest;
import com.enterprise.marketplace.notificationservice.dto.UpdateNotificationRequest;
import com.enterprise.marketplace.notificationservice.enums.NotificationStatus;
import com.enterprise.marketplace.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Enterprise notification lifecycle APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Idempotent
    @Operation(
            summary = "Create notification",
            description = "Creates a new notification record. Requires Idempotency-Key header.",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification created successfully"));
    }

    @PostMapping("/send")
    @Idempotent
    @Operation(
            summary = "Create and send notification immediately",
            security = @SecurityRequirement(name = HttpHeaders.IDEMPOTENCY_KEY))
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification sent successfully"));
    }

    @PostMapping("/retry")
    @Idempotent
    @Operation(summary = "Retry one or more failed notifications")
    public ResponseEntity<ApiResponse<RetryNotificationResponse>> retryNotifications(
            @Valid @RequestBody RetryNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.retryNotifications(request), "Notification retry processed"));
    }

    @GetMapping
    @Operation(summary = "List notifications")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> listNotifications(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.listNotifications(page, size)));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotification(notificationId)));
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Search notification by request ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getByRequestId(@PathVariable String requestId) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByRequestId(requestId)));
    }

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Search notifications by recipient ID")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getByRecipientId(
            @PathVariable String recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByRecipientId(recipientId, page, size)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Search notifications by status")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getByStatus(
            @PathVariable NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByStatus(status, page, size)));
    }

    @PutMapping("/{notificationId}")
    @Idempotent
    @Operation(summary = "Update notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateNotification(
            @PathVariable UUID notificationId, @Valid @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.updateNotification(notificationId, request), "Notification updated successfully"));
    }

    @PatchMapping("/{notificationId}/status")
    @Idempotent
    @Operation(summary = "Update notification status")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateStatus(
            @PathVariable UUID notificationId, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.updateStatus(notificationId, request), "Notification status updated successfully"));
    }

    @PostMapping("/{notificationId}/retry")
    @Idempotent
    @Operation(summary = "Retry failed notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> retryNotification(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationService.retryNotification(notificationId), "Notification retry scheduled successfully"));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Soft delete notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }
}
