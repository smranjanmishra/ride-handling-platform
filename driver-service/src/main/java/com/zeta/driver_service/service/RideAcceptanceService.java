package com.zeta.driver_service.service;

import com.zeta.driver_service.client.NotificationServiceClient;
import com.zeta.driver_service.client.RiderServiceClient;
import com.zeta.driver_service.dto.CompleteRideRequest;
import com.zeta.driver_service.dto.UpdateRideStatusRequest;
import com.zeta.driver_service.exception.DriverNotAvailableException;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideAcceptanceService {
    private final DriverRepository driverRepository;
    private final DriverAvailabilityRepository driverAvailabilityRepository;
    private final DriverAvailabilityService driverAvailabilityService;
    private final RiderServiceClient riderServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final AuthorizationService authorizationService;
    private final MetricsService metricsService;

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void acceptRide(Long driverId, Long rideId) {
        long startTime = System.currentTimeMillis();

        authorizationService.verifyDriverOwnership(driverId);

        if (!driverRepository.existsById(driverId)) {
            throw new DriverNotFoundException("Driver not found: " + driverId);
        }

        boolean isAvailable = driverAvailabilityRepository.findByDriverId(driverId)
                .map(a -> a.isAvailable())
                .orElse(false);

        if (!isAvailable) {
            metricsService.incrementRidesRejected();
            throw new DriverNotAvailableException("Driver is not available");
        }

        driverAvailabilityService.markDriverBusy(driverId, rideId);

        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status("DRIVER_ASSIGNED")
                .build();

        riderServiceClient.updateRideStatus(rideId, request);

        metricsService.incrementRidesAccepted();
        metricsService.recordRideAcceptanceTime(startTime);
        log.info("Driver {} accepted ride {}", driverId, rideId);
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void startRide(Long driverId, Long rideId) {
        authorizationService.verifyDriverOwnership(driverId);

        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status("STARTED")
                .startedAt(LocalDateTime.now())
                .build();

        riderServiceClient.updateRideStatus(rideId, request);
        log.info("Driver {} started ride {}", driverId, rideId);
    }

    @Transactional
    @Retryable(
            retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void completeRide(Long driverId, CompleteRideRequest request) {
        authorizationService.verifyDriverOwnership(driverId);

        UpdateRideStatusRequest statusRequest = UpdateRideStatusRequest.builder()
                .status("COMPLETED")
                .completedAt(LocalDateTime.now())
                .build();

        riderServiceClient.updateRideStatus(request.getRideId(), statusRequest);

        driverAvailabilityService.markDriverAvailable(driverId);

        metricsService.incrementRidesCompleted();
        log.info("Driver {} completed ride {}", driverId, request.getRideId());
    }
}
