package com.zeta.notification_service.controller;

import com.zeta.notification_service.dto.NotificationRequest;
import com.zeta.notification_service.dto.NotificationResponse;
import com.zeta.notification_service.dto.RideAcceptedNotificationRequest;
import com.zeta.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/ride-accepted")
    public ResponseEntity<NotificationResponse> sendRideAcceptedNotification(
            @Valid @RequestBody RideAcceptedNotificationRequest request) {

        NotificationResponse response =
                notificationService.sendRideAcceptedNotification(request.getRideId(), request.getDriverName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/ride-completed")
    public ResponseEntity<NotificationResponse> sendRideCompletedNotification(
            @Valid @RequestBody NotificationRequest request) {

        NotificationResponse response = notificationService.sendRideCompletedNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/payment-completed")
    public ResponseEntity<NotificationResponse> sendPaymentCompletedNotification(
            @Valid @RequestBody NotificationRequest request) {

        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
