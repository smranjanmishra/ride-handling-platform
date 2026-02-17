package com.zeta.notification_service.dto;

import com.zeta.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long userId;
    private Long rideId;
    private NotificationType type;
    private String message;
    private LocalDateTime sentAt;
}
