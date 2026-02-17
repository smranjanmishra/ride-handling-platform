package com.zeta.driver_service.controller;

import com.zeta.driver_service.dto.*;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.service.DriverAvailabilityService;
import com.zeta.driver_service.service.DriverLocationService;
import com.zeta.driver_service.service.DriverService;
import com.zeta.driver_service.service.RideAcceptanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;
    private final DriverLocationService driverLocationService;
    private final DriverAvailabilityService driverAvailabilityService;
    private final RideAcceptanceService rideAcceptanceService;

    @PostMapping("/register")
    public ResponseEntity<DriverDTO> registerDriver(
            @Valid @RequestBody CreateDriverRequest request) {
        log.info("REST request to register driver: {}", request.getEmail());
        DriverDTO driver = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(driver);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverDTO> getDriver(
            @PathVariable("driverId") Long driverId) {
        log.info("REST request to get driver: {}", driverId);
        DriverDTO driver = driverService.getDriver(driverId);
        return ResponseEntity.ok(driver);
    }

    @PutMapping("/{driverId}/location")
    public ResponseEntity<DriverLocationResponse> updateLocation(
            @PathVariable("driverId") Long driverId,
            @Valid @RequestBody UpdateLocationRequest request) {

        log.info("REST request to update location for driver: {}", driverId);

        Driver driver = driverService.updateLocation(
                driverId,
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(
                DriverLocationResponse.builder()
                        .driverId(driver.getDriverId())
                        .latitude(request.getLatitude())
                        .longitude(request.getLongitude())
                        .status(driver.getStatus())
                        .message("Driver location updated successfully")
                        .build()
        );
    }


    @PutMapping("/{driverId}/availability")
    public ResponseEntity<UpdateAvailabilityRequest> updateAvailability(
            @PathVariable("driverId") Long driverId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {

        log.info("REST request to update availability for driver: {}", driverId);

        boolean available = driverAvailabilityService.updateAvailability(driverId, request);

        return ResponseEntity.ok(
                UpdateAvailabilityRequest.builder()
                        .driverId(driverId)
                        .available(available)
                        .message("Driver availability updated successfully")
                        .build()
        );
    }


    @GetMapping("/available")
    public ResponseEntity<List<DriverDTO>> getAvailableDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "radiusKm", defaultValue = "10.0") Double radiusKm) {
        log.info("REST request to get available drivers near: {}, {}", latitude, longitude);
        List<DriverDTO> drivers = driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm);
        return ResponseEntity.ok(drivers);
    }

    @PostMapping("/{driverId}/accept-ride/{rideId}")
    public ResponseEntity<RideActionResponse> acceptRide(
            @PathVariable("driverId") Long driverId,
            @PathVariable("rideId") Long rideId) {

        log.info("REST request for driver {} to accept ride {}", driverId, rideId);

        rideAcceptanceService.acceptRide(driverId, rideId);

        return ResponseEntity.ok(
                RideActionResponse.builder()
                        .driverId(driverId)
                        .rideId(rideId)
                        .message("Driver accepted the ride successfully")
                        .build()
        );
    }


    @PostMapping("/{driverId}/start-ride/{rideId}")
    public ResponseEntity<RideActionResponse> startRide(
            @PathVariable("driverId") Long driverId,
            @PathVariable("rideId") Long rideId) {

        log.info("REST request for driver {} to start ride {}", driverId, rideId);

        rideAcceptanceService.startRide(driverId, rideId);

        return ResponseEntity.ok(
                RideActionResponse.builder()
                        .driverId(driverId)
                        .rideId(rideId)
                        .message("Driver started the ride successfully")
                        .build()
        );
    }


    @PostMapping("/{driverId}/complete-ride")
    public ResponseEntity<RideCompletionResponse> completeRide(
            @PathVariable("driverId") Long driverId,
            @Valid @RequestBody CompleteRideRequest request) {

        log.info("REST request for driver {} to complete ride {}", driverId, request.getRideId());

        rideAcceptanceService.completeRide(driverId, request);

        return ResponseEntity.ok(
                RideCompletionResponse.builder()
                        .driverId(driverId)
                        .rideId(request.getRideId())
                        .actualDistanceKm(request.getActualDistanceKm())
                        .actualFare(request.getActualFare())
                        .message("Ride completed successfully")
                        .build()
        );
    }


    @PostMapping("/{driverId}/notify-ride/{rideId}")
    public ResponseEntity<Void> notifyDriver(
            @PathVariable("driverId") Long driverId,
            @PathVariable("rideId") Long rideId) {
        log.info("Driver {} notified about ride {}", driverId, rideId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/{driverId}/notify-ride/{rideId}")
    public ResponseEntity<Void> notifyDriverInternal(
            @PathVariable("driverId") Long driverId,
            @PathVariable("rideId") Long rideId) {

        log.info("Internal notify: driver {} about ride {}", driverId, rideId);
        return ResponseEntity.ok().build();
    }

}
