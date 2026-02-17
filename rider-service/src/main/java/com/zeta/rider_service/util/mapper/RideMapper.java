package com.zeta.rider_service.util.mapper;

import com.zeta.rider_service.dto.RideDTO;
import com.zeta.rider_service.entity.Ride;

public class RideMapper {
    public static RideDTO toDTO(Ride ride) {
        if (ride == null) {
            return null;
        }

        return RideDTO.builder()
                .rideId(ride.getRideId())
                .riderId(ride.getRiderId())
                .driverId(ride.getDriverId())
                .pickupLatitude(ride.getPickupLatitude())
                .pickupLongitude(ride.getPickupLongitude())
                .pickupAddress(ride.getPickupAddress())
                .dropLatitude(ride.getDropLatitude())
                .dropLongitude(ride.getDropLongitude())
                .dropAddress(ride.getDropAddress())
                .status(ride.getStatus())
                .fareAmount(ride.getFareAmount())
                .distanceKm(ride.getDistanceKm())
                .requestedAt(ride.getRequestedAt())
                .assignedAt(ride.getAssignedAt())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .cancelledAt(ride.getCancelledAt())
                .cancellationReason(ride.getCancellationReason())
                .build();
    }
}

