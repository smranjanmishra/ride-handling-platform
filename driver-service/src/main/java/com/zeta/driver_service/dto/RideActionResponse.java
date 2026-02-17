package com.zeta.driver_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RideActionResponse {
    private Long driverId;
    private Long rideId;
    private String message;
}
