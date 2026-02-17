package com.zeta.driver_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RideCompletionResponse {
    private Long driverId;
    private Long rideId;
    private Double actualDistanceKm;
    private Double actualFare;
    private String message;
}
