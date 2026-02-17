package com.zeta.driver_service.controller;

import com.zeta.driver_service.dto.LoginRequest;
import com.zeta.driver_service.dto.LoginResponse;
import com.zeta.driver_service.service.DriverAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverAuthControllerTest {

    @Mock
    private DriverAuthService driverAuthService;

    @InjectMocks
    private DriverAuthController driverAuthController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("driver@example.com");
        loginRequest.setPassword("password123");

        loginResponse = LoginResponse.builder()
                .token("jwt-token-123")
                .userId(1L)
                .build();
    }

    @Test
    void testLogin_WhenValidRequest_ReturnsOkWithLoginResponse() {
        // Arrange
        when(driverAuthService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = driverAuthController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(loginResponse.getToken(), response.getBody().getToken());
        assertEquals(loginResponse.getUserId(), response.getBody().getUserId());
        verify(driverAuthService, times(1)).login(loginRequest);
    }

    @Test
    void testLogin_WhenServiceReturnsResponse_ReturnsCorrectResponse() {
        // Arrange
        LoginResponse customResponse = LoginResponse.builder()
                .token("custom-token")
                .userId(2L)
                .build();
        when(driverAuthService.login(any(LoginRequest.class))).thenReturn(customResponse);

        // Act
        ResponseEntity<LoginResponse> response = driverAuthController.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("custom-token", response.getBody().getToken());
        assertEquals(2L, response.getBody().getUserId());
        verify(driverAuthService, times(1)).login(loginRequest);
    }

    @Test
    void testLogin_WhenDifferentEmail_StillCallsService() {
        // Arrange
        LoginRequest differentRequest = new LoginRequest();
        differentRequest.setEmail("another@example.com");
        differentRequest.setPassword("password456");

        when(driverAuthService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act
        ResponseEntity<LoginResponse> response = driverAuthController.login(differentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(driverAuthService, times(1)).login(differentRequest);
    }
}
