package com.zeta.rider_service.controller;

import com.zeta.rider_service.dto.RideDTO;
import com.zeta.rider_service.dto.RideInternalDTO;
import com.zeta.rider_service.dto.UpdateRideStatusRequest;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/rides")
@RequiredArgsConstructor
@Slf4j
public class InternalRideController {

    private final RideService rideService;

    @PutMapping("/{rideId}/status")
    public ResponseEntity<RideDTO> updateRideStatus(
            @PathVariable("rideId") Long rideId,
            @Valid @RequestBody UpdateRideStatusRequest request) {
        log.info("Internal request to update ride {} status to {}", rideId, request.getStatus());
        RideDTO ride = rideService.updateRideStatus(rideId, request);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/{rideId}")
    public RideInternalDTO getRideInternal(@PathVariable("rideId") Long rideId) {
        log.info("Internal request to fetch ride {}", rideId);
        Ride ride = rideService.getRideEntity(rideId);
        return new RideInternalDTO(ride.getRideId(), ride.getRiderId());
    }
}