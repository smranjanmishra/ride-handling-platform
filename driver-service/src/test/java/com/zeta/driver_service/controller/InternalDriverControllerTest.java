package com.zeta.driver_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InternalDriverControllerTest {

    @InjectMocks
    private InternalDriverController internalDriverController;

    @Test
    void testNotifyDriver_WhenValidRequest_ReturnsOk() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        // Act
        ResponseEntity<Void> response = internalDriverController.notifyDriver(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testNotifyDriver_WhenDifferentDriverId_ReturnsOk() {
        // Arrange
        Long driverId = 999L;
        Long rideId = 200L;

        // Act
        ResponseEntity<Void> response = internalDriverController.notifyDriver(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testNotifyDriver_WhenDifferentRideId_ReturnsOk() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 500L;

        // Act
        ResponseEntity<Void> response = internalDriverController.notifyDriver(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
