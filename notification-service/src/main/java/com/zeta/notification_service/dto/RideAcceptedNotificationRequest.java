package com.zeta.notification_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RideAcceptedNotificationRequest {
    @NotNull
    private Long rideId;

    @NotNull
    private String driverName;
}
