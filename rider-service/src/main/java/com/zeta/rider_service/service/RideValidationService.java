package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.CreateRideRequest;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.InvalidRideRequestException;
import com.zeta.rider_service.exception.InvalidRideStateException;
import com.zeta.rider_service.exception.OngoingRideExistsException;
import com.zeta.rider_service.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideValidationService {
    private final RideRepository rideRepository;

    public void validateCreateRideRequest(CreateRideRequest request) {
        if (request.getPickupLatitude().equals(request.getDropLatitude()) &&
                request.getPickupLongitude().equals(request.getDropLongitude())) {
            throw new InvalidRideRequestException("Pickup and drop locations cannot be the same");
        }
        List<RideStatus> ongoingStatuses = List.of(
                RideStatus.REQUESTED,
                RideStatus.DRIVER_ASSIGNED,
                RideStatus.DRIVER_ARRIVED,
                RideStatus.STARTED
        );

        if (rideRepository.existsByRiderIdAndStatusIn(request.getRiderId(), ongoingStatuses)) {
            throw new OngoingRideExistsException("You already have an ongoing ride");
        }
    }

    public void validateCancellation(Ride ride) {
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStateException("Cannot cancel a completed ride");
        }

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new InvalidRideStateException("Ride is already cancelled");
        }

        if (ride.getStatus() == RideStatus.STARTED) {
            throw new InvalidRideStateException("Cannot cancel a ride that has already started");
        }
    }
}
