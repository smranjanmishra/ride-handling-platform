package com.zeta.rider_service.client;

import com.zeta.rider_service.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClient {

    private final RestClient restClient;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    @Async
    public void sendRideAccepted(Long rideId, String driverName) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .rideId(rideId)
                    .driverName(driverName)
                    .build();

            String url = notificationServiceUrl + "/api/notifications/ride-accepted";

            restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Ride accepted notification sent for ride {}", rideId);
        } catch (Exception e) {
            log.error("Failed to send ride accepted notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendRideCompleted(Long rideId) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .rideId(rideId)
                    .build();

            String url = notificationServiceUrl + "/api/notifications/ride-completed";

            restClient.post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Ride completed notification sent for ride {}", rideId);
        } catch (Exception e) {
            log.error("Failed to send ride completed notification: {}", e.getMessage());
        }
    }
}
