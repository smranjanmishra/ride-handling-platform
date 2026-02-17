package com.zeta.rider_service.dto;

import com.zeta.rider_service.enums.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideDTO {
    private Long rideId;
    private Long riderId;
    private Long driverId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;
    private Double dropLatitude;
    private Double dropLongitude;
    private String dropAddress;
    private RideStatus status;
    private BigDecimal fareAmount;
    private Double distanceKm;
    private LocalDateTime requestedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
