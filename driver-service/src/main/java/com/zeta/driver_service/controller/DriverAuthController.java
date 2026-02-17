package com.zeta.driver_service.controller;

import com.zeta.driver_service.dto.LoginRequest;
import com.zeta.driver_service.dto.LoginResponse;
import com.zeta.driver_service.service.DriverAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class DriverAuthController {
    private final DriverAuthService driverAuthService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Driver login request for email: {}", request.getEmail());
        LoginResponse response = driverAuthService.login(request);
        return ResponseEntity.ok(response);
    }
}
