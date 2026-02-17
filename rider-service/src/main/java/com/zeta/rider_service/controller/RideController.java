package com.zeta.rider_service.controller;

import com.zeta.rider_service.dto.*;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Slf4j
public class RideController {
    private final RideService rideService;

    @PostMapping("/book")
    public ResponseEntity<RideDTO> createRide(
            @Valid @RequestBody CreateRideRequest request) {
        log.info("REST request to create ride for rider: {}", request.getRiderId());
        RideDTO ride = rideService.createRide(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ride);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideDTO> getRide(
            @PathVariable("rideId") Long rideId) {
        log.info("REST request to get ride: {}", rideId);
        RideDTO ride = rideService.getRide(rideId);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideDTO> cancelRide(
            @PathVariable("rideId") Long rideId,
            @Valid @RequestBody CancelRideRequest request) {
        log.info("REST request to cancel ride: {}", rideId);
        RideDTO ride = rideService.cancelRide(rideId, request);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/nearby-cabs")
    public ResponseEntity<List<DriverDTO>> getNearbyCabs(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "radiusKm", defaultValue = "5.0") Double radiusKm) {
        log.info("REST request to get nearby cabs at: {}, {}", latitude, longitude);
        List<DriverDTO> cabs = rideService.getNearbyCabs(latitude, longitude, radiusKm);
        return ResponseEntity.ok(cabs);
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<RideDTO>> getRidesByRider(
            @PathVariable("riderId") Long riderId) {
        log.info("REST request to get rides for rider: {}", riderId);
        List<RideDTO> rides = rideService.getRidesByRider(riderId);
        return ResponseEntity.ok(rides);
    }

}
