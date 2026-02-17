package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.LoginRequest;
import com.zeta.driver_service.dto.LoginResponse;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.enums.DriverStatus;
import com.zeta.driver_service.exception.InvalidCredentialsException;
import com.zeta.driver_service.repository.DriverRepository;
import com.zeta.driver_service.security.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverAuthService {
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Driver driver = driverRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), driver.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new InvalidCredentialsException("Driver account is not active");
        }

        String token = jwtTokenProvider.generateToken(driver.getDriverId(), "DRIVER");

        log.info("Driver logged in successfully: {}", driver.getDriverId());

        return LoginResponse.builder()
                .token(token)
                .userId(driver.getDriverId())
                .build();
    }
}
