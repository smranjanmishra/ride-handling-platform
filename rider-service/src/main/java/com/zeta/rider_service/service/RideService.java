package com.zeta.rider_service.service;

import com.zeta.rider_service.client.DriverServiceClient;
import com.zeta.rider_service.client.NotificationServiceClient;
import com.zeta.rider_service.dto.*;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.repository.RideRepository;
import com.zeta.rider_service.statemachine.RideStateMachine;
import com.zeta.rider_service.util.mapper.RideMapper;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {

    private final RideRepository rideRepository;
    private final RiderService riderService;
    private final RideValidationService rideValidationService;
    private final DriverServiceClient driverServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RideStateMachine rideStateMachine;
    private final AuthorizationService authorizationService;
    private final MetricsService metricsService;

    @Value("${pricing.base-fare:50.0}")
    private Double baseFare;

    @Value("${pricing.per-km-rate:10.0}")
    private Double perKmRate;

    @Value("${driver.search-radius-km:10.0}")
    private Double searchRadiusKm;

    @Transactional
    public RideDTO createRide(CreateRideRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Creating ride for rider: {}", request.getRiderId());

            authorizationService.verifyRiderOwnership(request.getRiderId());

            //Check business rules FIRST (ongoing rides ..)
            rideValidationService.validateCreateRideRequest(request);

            // THEN check idempotency (duplicate request prevention)
            String idempotencyKey = request.getIdempotencyKey() != null
                    ? request.getIdempotencyKey()
                    : UUID.randomUUID().toString();

            if (rideRepository.existsByIdempotencyKey(idempotencyKey)) {
                log.info("Duplicate ride request with key: {}", idempotencyKey);
                Ride existingRide = rideRepository.findByIdempotencyKey(idempotencyKey);
                metricsService.recordRideCreationTime(startTime);
                return RideMapper.toDTO(existingRide);
            }

            List<DriverDTO> availableDrivers = driverServiceClient.getAvailableDrivers(
                    request.getPickupLatitude(),
                    request.getPickupLongitude(),
                    searchRadiusKm
            );

            if (availableDrivers.isEmpty()) {
                log.warn("No drivers available for rider {}", request.getRiderId());
                metricsService.incrementAssignmentsFailed();
                throw new IllegalStateException("No drivers available nearby");
            }

            DriverDTO assignedDriver = availableDrivers.get(0);

            double approximateDistance = calculateApproximateDistance(
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    request.getDropLatitude(), request.getDropLongitude()
            );

            BigDecimal fare = BigDecimal.valueOf(baseFare + (approximateDistance * perKmRate));

            Ride ride = Ride.builder()
                    .riderId(request.getRiderId())
                    .driverId(assignedDriver.getDriverId())
                    .pickupLatitude(request.getPickupLatitude())
                    .pickupLongitude(request.getPickupLongitude())
                    .pickupAddress(request.getPickupAddress())
                    .dropLatitude(request.getDropLatitude())
                    .dropLongitude(request.getDropLongitude())
                    .dropAddress(request.getDropAddress())
                    .status(RideStatus.REQUESTED)
                    .fareAmount(fare)
                    .distanceKm(approximateDistance)
                    .requestedAt(LocalDateTime.now())
                    .assignedAt(LocalDateTime.now())
                    .idempotencyKey(idempotencyKey)
                    .build();

            Ride savedRide = rideRepository.save(ride);
            log.info("Ride {} created with driver {}", savedRide.getRideId(), assignedDriver.getDriverId());

            try {
                driverServiceClient.notifyDriver(assignedDriver.getDriverId(),
                        savedRide.getRideId());
            }
            catch (Exception e) {
                log.error("Failed to notify driver {}: {}", assignedDriver.getDriverId(), e.getMessage());
            }

            // Notify rider that driver is assigned
            notificationServiceClient.sendRideAccepted(
                    savedRide.getRideId(),
                    assignedDriver.getName()
            );

            metricsService.incrementRidesCreated();
            metricsService.recordRideCreationTime(startTime);


            return RideMapper.toDTO(savedRide);

        } catch (Exception e) {
            log.error("Error creating ride: {}", e.getMessage(), e);
            metricsService.incrementValidationFailures();
            throw e;
        }
    }

    private double calculateApproximateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double latDiff = lat2 - lat1;
        double lonDiff = lon2 - lon1;
        double distanceKm = Math.sqrt(Math.pow(latDiff * 111, 2) + Math.pow(lonDiff * 111, 2));
        log.debug("Approximate distance calculated: {} km", distanceKm);
        return distanceKm;
    }

    @Transactional(readOnly = true)
    public RideDTO getRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));

        authorizationService.verifyRideAccess(ride);
        return RideMapper.toDTO(ride);
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public RideDTO cancelRide(Long rideId, CancelRideRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));

        authorizationService.verifyRideAccess(ride);
        rideValidationService.validateCancellation(ride);
        rideStateMachine.validateTransition(ride.getStatus(), RideStatus.CANCELLED);

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(LocalDateTime.now());
        ride.setCancellationReason(request.getCancellationReason());

        Ride updated = rideRepository.save(ride);

        metricsService.incrementRidesCancelled();
        log.info("Ride {} cancelled", rideId);

        return RideMapper.toDTO(updated);
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public RideDTO updateRideStatus(Long rideId, UpdateRideStatusRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));

        if (authorizationService.isDriver()) {
            if (!authorizationService.getCurrentUserId().equals(ride.getDriverId())) {
                throw new AccessDeniedException("You can only update rides assigned to you");
            }
        }

        RideStatus nextStatus = rideStateMachine.validateAndGetNextStatus(
                ride.getStatus(), request.getStatus());

        ride.setStatus(nextStatus);

        if (nextStatus == RideStatus.STARTED) {
            ride.setStartedAt(request.getStartedAt() != null
                    ? request.getStartedAt()
                    : LocalDateTime.now());
        } else if (nextStatus == RideStatus.COMPLETED) {
            ride.setCompletedAt(request.getCompletedAt() != null
                    ? request.getCompletedAt()
                    : LocalDateTime.now());
            riderService.incrementTotalRides(ride.getRiderId());
            metricsService.incrementRidesCompleted();

            // Notify rider that ride is completed
            notificationServiceClient.sendRideCompleted(rideId);
        }

        Ride updated = rideRepository.save(ride);
        return RideMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<RideDTO> getRidesByRider(Long riderId) {
        authorizationService.verifyRiderOwnership(riderId);

        return rideRepository.findByRiderIdOrderByRequestedAtDesc(riderId)
                .stream()
                .map(RideMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DriverDTO> getNearbyCabs(Double latitude, Double longitude, Double radiusKm) {
        return driverServiceClient.getAvailableDrivers(latitude, longitude, radiusKm);
    }

    @Transactional(readOnly = true)
    public Ride getRideEntity(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));
    }
}