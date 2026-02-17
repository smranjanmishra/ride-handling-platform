package com.zeta.driver_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteRideRequest {
    @NotNull
    private Long rideId;

    @NotNull
    @Positive
    private Double actualDistanceKm;

    @NotNull
    @Positive
    private Double actualFare;
}
