package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.DriverDTO;
import com.zeta.driver_service.dto.UpdateAvailabilityRequest;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.entity.DriverAvailability;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
import com.zeta.driver_service.util.DistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverAvailabilityServiceTest {

    @Mock
    private DriverAvailabilityRepository driverAvailabilityRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverLocationRepository driverLocationRepository;

    @Mock
    private DistanceCalculator distanceCalculator;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private DriverAvailabilityService driverAvailabilityService;

    private DriverAvailability driverAvailability;
    private UpdateAvailabilityRequest updateAvailabilityRequest;

    @BeforeEach
    void setUp() {
        driverAvailability = DriverAvailability.builder()
                .id(1L)
                .driverId(1L)
                .available(false)
                .currentRideId(null)
                .build();

        updateAvailabilityRequest = UpdateAvailabilityRequest.builder()
                .driverId(1L)
                .available(true)
                .build();
    }

    @Test
    void testUpdateAvailability_WhenDriverExists_ReturnsUpdatedAvailability() {
        // Arrange
        when(driverAvailabilityRepository.findByDriverId(1L)).thenReturn(Optional.of(driverAvailability));
        when(driverAvailabilityRepository.save(any(DriverAvailability.class))).thenReturn(driverAvailability);

        // Act
        boolean result = driverAvailabilityService.updateAvailability(1L, updateAvailabilityRequest);

        // Assert
        assertTrue(result);
        assertTrue(driverAvailability.isAvailable());
        verify(driverAvailabilityRepository).findByDriverId(1L);
        verify(driverAvailabilityRepository).save(driverAvailability);
    }

    @Test
    void testUpdateAvailability_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        when(driverAvailabilityRepository.findByDriverId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverAvailabilityService.updateAvailability(1L, updateAvailabilityRequest));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(driverAvailabilityRepository).findByDriverId(1L);
        verify(driverAvailabilityRepository, never()).save(any());
    }

    @Test
    void testUpdateAvailability_WhenSettingToUnavailable_ReturnsFalse() {
        // Arrange
        updateAvailabilityRequest.setAvailable(false);
        when(driverAvailabilityRepository.findByDriverId(1L)).thenReturn(Optional.of(driverAvailability));
        when(driverAvailabilityRepository.save(any(DriverAvailability.class))).thenReturn(driverAvailability);

        // Act
        boolean result = driverAvailabilityService.updateAvailability(1L, updateAvailabilityRequest);

        // Assert
        assertFalse(result);
        assertFalse(driverAvailability.isAvailable());
        verify(driverAvailabilityRepository).findByDriverId(1L);
        verify(driverAvailabilityRepository).save(driverAvailability);
    }

    @Test
    void testGetAvailableDrivers_WhenDriversWithinRadius_ReturnsList() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        DriverAvailability availability1 = DriverAvailability.builder()
                .driverId(1L)
                .available(true)
                .build();

        DriverAvailability availability2 = DriverAvailability.builder()
                .driverId(2L)
                .available(true)
                .build();

        List<DriverAvailability> availableDrivers = new ArrayList<>();
        availableDrivers.add(availability1);
        availableDrivers.add(availability2);

        DriverLocation location1 = DriverLocation.builder()
                .driverId(1L)
                .latitude(40.7130)
                .longitude(-74.0062)
                .build();

        DriverLocation location2 = DriverLocation.builder()
                .driverId(2L)
                .latitude(40.7140)
                .longitude(-74.0070)
                .build();

        Driver driver1 = Driver.builder()
                .driverId(1L)
                .name("Driver 1")
                .email("driver1@example.com")
                .phone("1111111111")
                .licenseNumber("LIC1")
                .vehicleNumber("VEH1")
                .vehicleType("SEDAN")
                .totalRides(5)
                .build();

        Driver driver2 = Driver.builder()
                .driverId(2L)
                .name("Driver 2")
                .email("driver2@example.com")
                .phone("2222222222")
                .licenseNumber("LIC2")
                .vehicleNumber("VEH2")
                .vehicleType("SUV")
                .totalRides(10)
                .build();

        when(driverAvailabilityRepository.findByAvailable(true)).thenReturn(availableDrivers);
        when(driverLocationRepository.findByDriverId(1L)).thenReturn(Optional.of(location1));
        when(driverLocationRepository.findByDriverId(2L)).thenReturn(Optional.of(location2));
        when(distanceCalculator.calculateDistance(latitude, longitude, location1.getLatitude(), location1.getLongitude()))
                .thenReturn(2.0);
        when(distanceCalculator.calculateDistance(latitude, longitude, location2.getLatitude(), location2.getLongitude()))
                .thenReturn(3.0);
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver1));
        when(driverRepository.findById(2L)).thenReturn(Optional.of(driver2));

        // Act
        List<DriverDTO> result = driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(driverAvailabilityRepository).findByAvailable(true);
        verify(driverLocationRepository).findByDriverId(1L);
        verify(driverLocationRepository).findByDriverId(2L);
        verify(distanceCalculator, times(2)).calculateDistance(any(), any(), any(), any());
        verify(driverRepository).findById(1L);
        verify(driverRepository).findById(2L);
    }

    @Test
    void testGetAvailableDrivers_WhenNoDriversWithinRadius_ReturnsEmptyList() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        DriverAvailability availability = DriverAvailability.builder()
                .driverId(1L)
                .available(true)
                .build();

        List<DriverAvailability> availableDrivers = new ArrayList<>();
        availableDrivers.add(availability);

        DriverLocation location = DriverLocation.builder()
                .driverId(1L)
                .latitude(40.8000)
                .longitude(-74.1000)
                .build();

        when(driverAvailabilityRepository.findByAvailable(true)).thenReturn(availableDrivers);
        when(driverLocationRepository.findByDriverId(1L)).thenReturn(Optional.of(location));
        when(distanceCalculator.calculateDistance(latitude, longitude, location.getLatitude(), location.getLongitude()))
                .thenReturn(10.0);

        // Act
        List<DriverDTO> result = driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(driverAvailabilityRepository).findByAvailable(true);
        verify(driverLocationRepository).findByDriverId(1L);
        verify(distanceCalculator).calculateDistance(any(), any(), any(), any());
        verify(driverRepository, never()).findById(any());
    }

    @Test
    void testGetAvailableDrivers_WhenDriverHasNoLocation_ExcludesFromResult() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        DriverAvailability availability = DriverAvailability.builder()
                .driverId(1L)
                .available(true)
                .build();

        List<DriverAvailability> availableDrivers = new ArrayList<>();
        availableDrivers.add(availability);

        when(driverAvailabilityRepository.findByAvailable(true)).thenReturn(availableDrivers);
        when(driverLocationRepository.findByDriverId(1L)).thenReturn(Optional.empty());

        // Act
        List<DriverDTO> result = driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(driverAvailabilityRepository).findByAvailable(true);
        verify(driverLocationRepository).findByDriverId(1L);
        verify(distanceCalculator, never()).calculateDistance(any(), any(), any(), any());
        verify(driverRepository, never()).findById(any());
    }

    @Test
    void testGetAvailableDrivers_WhenDriverNotFound_ExcludesFromResult() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        DriverAvailability availability = DriverAvailability.builder()
                .driverId(1L)
                .available(true)
                .build();

        List<DriverAvailability> availableDrivers = new ArrayList<>();
        availableDrivers.add(availability);

        DriverLocation location = DriverLocation.builder()
                .driverId(1L)
                .latitude(40.7130)
                .longitude(-74.0062)
                .build();

        when(driverAvailabilityRepository.findByAvailable(true)).thenReturn(availableDrivers);
        when(driverLocationRepository.findByDriverId(1L)).thenReturn(Optional.of(location));
        when(distanceCalculator.calculateDistance(latitude, longitude, location.getLatitude(), location.getLongitude()))
                .thenReturn(2.0);
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        List<DriverDTO> result = driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(driverAvailabilityRepository).findByAvailable(true);
        verify(driverLocationRepository).findByDriverId(1L);
        verify(distanceCalculator).calculateDistance(any(), any(), any(), any());
        verify(driverRepository).findById(1L);
    }

    @Test
    void testMarkDriverBusy_WhenDriverExists_UpdatesAvailability() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(driverAvailability));
        when(driverAvailabilityRepository.save(any(DriverAvailability.class))).thenReturn(driverAvailability);

        // Act
        driverAvailabilityService.markDriverBusy(driverId, rideId);

        // Assert
        assertFalse(driverAvailability.isAvailable());
        assertEquals(rideId, driverAvailability.getCurrentRideId());
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository).save(driverAvailability);
    }

    @Test
    void testMarkDriverBusy_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverAvailabilityService.markDriverBusy(driverId, rideId));
        assertEquals("Driver availability not found: 1", exception.getMessage());
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository, never()).save(any());
    }

    @Test
    void testMarkDriverAvailable_WhenDriverExists_UpdatesAvailability() {
        // Arrange
        Long driverId = 1L;
        driverAvailability.setAvailable(false);
        driverAvailability.setCurrentRideId(100L);
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(driverAvailability));
        when(driverAvailabilityRepository.save(any(DriverAvailability.class))).thenReturn(driverAvailability);

        // Act
        driverAvailabilityService.markDriverAvailable(driverId);

        // Assert
        assertTrue(driverAvailability.isAvailable());
        assertNull(driverAvailability.getCurrentRideId());
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository).save(driverAvailability);
    }

    @Test
    void testMarkDriverAvailable_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverAvailabilityService.markDriverAvailable(driverId));
        assertEquals("Driver availability not found: 1", exception.getMessage());
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository, never()).save(any());
    }
}
