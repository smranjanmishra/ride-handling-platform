package com.zeta.notification_service.service;

import com.zeta.notification_service.client.RiderClient;
import com.zeta.notification_service.dto.NotificationRequest;
import com.zeta.notification_service.dto.NotificationResponse;
import com.zeta.notification_service.entity.Notification;
import com.zeta.notification_service.entity.RideDetails;
import com.zeta.notification_service.enums.NotificationType;
import com.zeta.notification_service.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final RiderClient riderClient;

    @Transactional
    public NotificationResponse sendRideAcceptedNotification(Long rideId, String driverName) {

        RideDetails ride = riderClient.getRide(rideId);
        Long userId = ride.getRiderId(); // authoritative

        String message = String.format(
                "Your ride #%d has been accepted by driver %s",
                rideId, driverName
        );

        return saveAndSendNotification(
                userId,
                rideId,
                NotificationType.RIDE_ACCEPTED,
                message
        );
    }


    @Transactional
    public NotificationResponse sendRideCompletedNotification(NotificationRequest request) {
        String message = String.format("Your ride #%d has been completed. Thank you for riding with us!",
                request.getRideId());

        return saveAndSendNotification(request.getUserId(), request.getRideId(),
                NotificationType.RIDE_COMPLETED, message);
    }

    @Transactional
    public NotificationResponse sendPaymentCompletedNotification(NotificationRequest request) {
        String message = String.format("Payment of ₹%.2f for ride #%d has been completed successfully",
                request.getAmount(), request.getRideId());

        return saveAndSendNotification(request.getUserId(), request.getRideId(),
                NotificationType.PAYMENT_COMPLETED, message);
    }

    private NotificationResponse saveAndSendNotification(Long userId, Long rideId,
                                                         NotificationType type, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .rideId(rideId)
                .type(type)
                .message(message)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification sent: {} for userId: {}, rideId: {}", type, userId, rideId);

        return NotificationResponse.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .rideId(saved.getRideId())
                .type(saved.getType())
                .message(saved.getMessage())
                .sentAt(saved.getSentAt())
                .build();
    }
}
