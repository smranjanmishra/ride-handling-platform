package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.LoginRequest;
import com.zeta.driver_service.dto.LoginResponse;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.enums.DriverStatus;
import com.zeta.driver_service.exception.InvalidCredentialsException;
import com.zeta.driver_service.repository.DriverRepository;
import com.zeta.driver_service.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverAuthServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private DriverAuthService driverAuthService;

    private Driver testDriver;
    private LoginRequest loginRequest;
    private String passwordHash = "$2a$10$encodedPasswordHash";

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .driverId(1L)
                .email("driver@example.com")
                .passwordHash(passwordHash)
                .status(DriverStatus.ACTIVE)
                .name("Test Driver")
                .phone("1234567890")
                .licenseNumber("LIC123")
                .vehicleNumber("VEH123")
                .vehicleType("SEDAN")
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("driver@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLogin_WhenValidCredentials_ReturnsLoginResponse() {
        // Arrange
        String token = "jwt-token-123";
        when(driverRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches(loginRequest.getPassword(), passwordHash)).thenReturn(true);
        when(jwtTokenProvider.generateToken(testDriver.getDriverId(), "DRIVER")).thenReturn(token);

        // Act
        LoginResponse response = driverAuthService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.getToken());
        assertEquals(testDriver.getDriverId(), response.getUserId());
        verify(driverRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), passwordHash);
        verify(jwtTokenProvider).generateToken(testDriver.getDriverId(), "DRIVER");
    }

    @Test
    void testLogin_WhenDriverNotFound_ThrowsInvalidCredentialsException() {
        // Arrange
        when(driverRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> driverAuthService.login(loginRequest));
        assertEquals("Invalid email or password", exception.getMessage());
        verify(driverRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void testLogin_WhenInvalidPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        when(driverRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches(loginRequest.getPassword(), passwordHash)).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> driverAuthService.login(loginRequest));
        assertEquals("Invalid email or password", exception.getMessage());
        verify(driverRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), passwordHash);
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void testLogin_WhenDriverNotActive_ThrowsInvalidCredentialsException() {
        // Arrange
        testDriver.setStatus(DriverStatus.INACTIVE);
        when(driverRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches(loginRequest.getPassword(), passwordHash)).thenReturn(true);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> driverAuthService.login(loginRequest));
        assertEquals("Driver account is not active", exception.getMessage());
        verify(driverRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), passwordHash);
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }

    @Test
    void testLogin_WhenDriverSuspended_ThrowsInvalidCredentialsException() {
        // Arrange
        testDriver.setStatus(DriverStatus.SUSPENDED);
        when(driverRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches(loginRequest.getPassword(), passwordHash)).thenReturn(true);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> driverAuthService.login(loginRequest));
        assertEquals("Driver account is not active", exception.getMessage());
        verify(driverRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), passwordHash);
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }
}
