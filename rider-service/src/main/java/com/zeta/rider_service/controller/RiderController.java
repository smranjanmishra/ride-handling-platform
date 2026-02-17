package com.zeta.rider_service.controller;

import com.zeta.rider_service.dto.CreateRiderRequest;
import com.zeta.rider_service.dto.RiderDTO;
import com.zeta.rider_service.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riders")
@RequiredArgsConstructor
@Slf4j
public class RiderController {

    private final RiderService riderService;

    @PostMapping("/register")
    public ResponseEntity<RiderDTO> registerRider(
            @Valid @RequestBody CreateRiderRequest request) {
        log.info("REST request to register rider: {}", request.getEmail());
        RiderDTO rider = riderService.createRider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rider);
    }

    @GetMapping("/{riderId}")
    public ResponseEntity<RiderDTO> getRider(
            @PathVariable("riderId") Long riderId) {
        log.info("REST request to get rider: {}", riderId);
        RiderDTO rider = riderService.getRider(riderId);
        return ResponseEntity.ok(rider);
    }
}
