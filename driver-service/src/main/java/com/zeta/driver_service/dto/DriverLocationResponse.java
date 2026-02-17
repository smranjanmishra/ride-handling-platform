package com.zeta.driver_service.dto;

import com.zeta.driver_service.enums.DriverStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DriverLocationResponse {
    private Long driverId;
    private double latitude;
    private double longitude;
    private DriverStatus status;
    private String message;
}
