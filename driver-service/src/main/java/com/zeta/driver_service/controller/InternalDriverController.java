package com.zeta.driver_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/drivers")
@Slf4j
public class InternalDriverController {
    @PostMapping("/{driverId}/notify-ride/{rideId}")
    public ResponseEntity<Void> notifyDriver(
            @PathVariable Long driverId,
            @PathVariable Long rideId) {

        log.info("Internal: Notifying driver {} about ride {}", driverId, rideId);
        return ResponseEntity.ok().build();
    }

}
