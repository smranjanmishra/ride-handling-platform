package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.UpdateLocationRequest;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverLocationServiceTest {

    @Mock
    private DriverLocationRepository driverLocationRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private DriverLocationService driverLocationService;

    private UpdateLocationRequest updateLocationRequest;
    private DriverLocation existingLocation;

    @BeforeEach
    void setUp() {
        updateLocationRequest = UpdateLocationRequest.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        existingLocation = DriverLocation.builder()
                .locationId(1L)
                .driverId(1L)
                .latitude(40.7000)
                .longitude(-74.0000)
                .build();
    }

    @Test
    void testUpdateLocation_WhenLocationExists_UpdatesLocation() {
        // Arrange
        Long driverId = 1L;
        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(true);
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.of(existingLocation));
        when(driverLocationRepository.save(any(DriverLocation.class))).thenReturn(existingLocation);

        // Act
        driverLocationService.updateLocation(driverId, updateLocationRequest);

        // Assert
        assertEquals(updateLocationRequest.getLatitude(), existingLocation.getLatitude());
        assertEquals(updateLocationRequest.getLongitude(), existingLocation.getLongitude());
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverLocationRepository).save(existingLocation);
    }

    @Test
    void testUpdateLocation_WhenLocationDoesNotExist_CreatesNewLocation() {
        // Arrange
        Long driverId = 1L;
        DriverLocation newLocation = DriverLocation.builder()
                .driverId(driverId)
                .latitude(updateLocationRequest.getLatitude())
                .longitude(updateLocationRequest.getLongitude())
                .build();

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(true);
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.empty());
        when(driverLocationRepository.save(any(DriverLocation.class))).thenReturn(newLocation);

        // Act
        driverLocationService.updateLocation(driverId, updateLocationRequest);

        // Assert
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverLocationRepository).save(any(DriverLocation.class));
    }

    @Test
    void testUpdateLocation_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(false);

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverLocationService.updateLocation(driverId, updateLocationRequest));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverLocationRepository, never()).findByDriverId(any());
        verify(driverLocationRepository, never()).save(any());
    }
}
