package com.zeta.driver_service.client;

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
    public void sendNotificationAsync(Long userId, String userType, String eventType, Long rideId) {
        try {
            String url = notificationServiceUrl + "/api/v1/notifications/send";

            restClient.post()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notification sent to {} (type: {}) for event: {}", userId, userType, eventType);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}