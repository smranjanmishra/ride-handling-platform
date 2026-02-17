package com.zeta.driver_service.controller;

import com.zeta.driver_service.dto.*;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.enums.DriverStatus;
import com.zeta.driver_service.service.DriverAvailabilityService;
import com.zeta.driver_service.service.DriverLocationService;
import com.zeta.driver_service.service.DriverService;
import com.zeta.driver_service.service.RideAcceptanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @Mock
    private DriverLocationService driverLocationService;

    @Mock
    private DriverAvailabilityService driverAvailabilityService;

    @Mock
    private RideAcceptanceService rideAcceptanceService;

    @InjectMocks
    private DriverController driverController;

    private CreateDriverRequest createDriverRequest;
    private DriverDTO driverDTO;
    private Driver driver;
    private UpdateLocationRequest updateLocationRequest;
    private UpdateAvailabilityRequest updateAvailabilityRequest;
    private CompleteRideRequest completeRideRequest;

    @BeforeEach
    void setUp() {
        createDriverRequest = CreateDriverRequest.builder()
                .name("Test Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .password("password123")
                .licenseNumber("LIC123")
                .vehicleNumber("VEH123")
                .vehicleType("SEDAN")
                .build();

        driverDTO = DriverDTO.builder()
                .driverId(1L)
                .name("Test Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC123")
                .vehicleNumber("VEH123")
                .vehicleType("SEDAN")
                .totalRides(0)
                .latitude(40.7128)
                .longitude(-74.0060)
                .available(true)
                .build();

        driver = Driver.builder()
                .driverId(1L)
                .name("Test Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .licenseNumber("LIC123")
                .vehicleNumber("VEH123")
                .vehicleType("SEDAN")
                .status(DriverStatus.ACTIVE)
                .totalRides(0)
                .build();

        updateLocationRequest = UpdateLocationRequest.builder()
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        updateAvailabilityRequest = UpdateAvailabilityRequest.builder()
                .driverId(1L)
                .available(true)
                .build();

        completeRideRequest = CompleteRideRequest.builder()
                .rideId(100L)
                .actualDistanceKm(10.5)
                .actualFare(250.0)
                .build();
    }

    @Test
    void testRegisterDriver_WhenValidRequest_ReturnsCreatedWithDriverDTO() {
        // Arrange
        when(driverService.createDriver(any(CreateDriverRequest.class))).thenReturn(driverDTO);

        // Act
        ResponseEntity<DriverDTO> response = driverController.registerDriver(createDriverRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverDTO.getDriverId(), response.getBody().getDriverId());
        assertEquals(driverDTO.getEmail(), response.getBody().getEmail());
        verify(driverService, times(1)).createDriver(createDriverRequest);
    }

    @Test
    void testGetDriver_WhenDriverExists_ReturnsOkWithDriverDTO() {
        // Arrange
        Long driverId = 1L;
        when(driverService.getDriver(driverId)).thenReturn(driverDTO);

        // Act
        ResponseEntity<DriverDTO> response = driverController.getDriver(driverId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverDTO.getDriverId(), response.getBody().getDriverId());
        assertEquals(driverDTO.getName(), response.getBody().getName());
        verify(driverService, times(1)).getDriver(driverId);
    }

    @Test
    void testUpdateLocation_WhenValidRequest_ReturnsOkWithLocationResponse() {
        // Arrange
        Long driverId = 1L;
        when(driverService.updateLocation(eq(driverId), eq(updateLocationRequest.getLatitude()), eq(updateLocationRequest.getLongitude())))
                .thenReturn(driver);

        // Act
        ResponseEntity<DriverLocationResponse> response = driverController.updateLocation(driverId, updateLocationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverId, response.getBody().getDriverId());
        assertEquals(updateLocationRequest.getLatitude(), response.getBody().getLatitude());
        assertEquals(updateLocationRequest.getLongitude(), response.getBody().getLongitude());
        assertEquals(driver.getStatus(), response.getBody().getStatus());
        assertEquals("Driver location updated successfully", response.getBody().getMessage());
        verify(driverService, times(1)).updateLocation(driverId, updateLocationRequest.getLatitude(), updateLocationRequest.getLongitude());
    }

    @Test
    void testUpdateAvailability_WhenValidRequest_ReturnsOkWithAvailabilityResponse() {
        // Arrange
        Long driverId = 1L;
        boolean available = true;
        when(driverAvailabilityService.updateAvailability(eq(driverId), any(UpdateAvailabilityRequest.class)))
                .thenReturn(available);

        // Act
        ResponseEntity<UpdateAvailabilityRequest> response = driverController.updateAvailability(driverId, updateAvailabilityRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverId, response.getBody().getDriverId());
        assertEquals(available, response.getBody().isAvailable());
        assertEquals("Driver availability updated successfully", response.getBody().getMessage());
        verify(driverAvailabilityService, times(1)).updateAvailability(driverId, updateAvailabilityRequest);
    }

    @Test
    void testUpdateAvailability_WhenSetToUnavailable_ReturnsCorrectResponse() {
        // Arrange
        Long driverId = 1L;
        boolean available = false;
        UpdateAvailabilityRequest request = UpdateAvailabilityRequest.builder()
                .driverId(driverId)
                .available(false)
                .build();
        when(driverAvailabilityService.updateAvailability(eq(driverId), any(UpdateAvailabilityRequest.class)))
                .thenReturn(available);

        // Act
        ResponseEntity<UpdateAvailabilityRequest> response = driverController.updateAvailability(driverId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isAvailable());
        verify(driverAvailabilityService, times(1)).updateAvailability(driverId, request);
    }

    @Test
    void testGetAvailableDrivers_WhenDriversExist_ReturnsOkWithDriverList() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 10.0;
        List<DriverDTO> availableDrivers = Arrays.asList(driverDTO);

        when(driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm))
                .thenReturn(availableDrivers);

        // Act
        ResponseEntity<List<DriverDTO>> response = driverController.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(driverDTO.getDriverId(), response.getBody().get(0).getDriverId());
        verify(driverAvailabilityService, times(1)).getAvailableDrivers(latitude, longitude, radiusKm);
    }

    @Test
    void testGetAvailableDrivers_WhenNoDrivers_ReturnsEmptyList() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 10.0;
        List<DriverDTO> emptyList = Arrays.asList();

        when(driverAvailabilityService.getAvailableDrivers(latitude, longitude, radiusKm))
                .thenReturn(emptyList);

        // Act
        ResponseEntity<List<DriverDTO>> response = driverController.getAvailableDrivers(latitude, longitude, radiusKm);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(driverAvailabilityService, times(1)).getAvailableDrivers(latitude, longitude, radiusKm);
    }

    @Test
    void testGetAvailableDrivers_WhenDefaultRadius_UsesDefaultValue() {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double defaultRadius = 10.0;
        List<DriverDTO> drivers = Arrays.asList(driverDTO);

        when(driverAvailabilityService.getAvailableDrivers(latitude, longitude, defaultRadius))
                .thenReturn(drivers);

        // Act
        ResponseEntity<List<DriverDTO>> response = driverController.getAvailableDrivers(latitude, longitude, defaultRadius);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(driverAvailabilityService, times(1)).getAvailableDrivers(latitude, longitude, defaultRadius);
    }

    @Test
    void testAcceptRide_WhenValidRequest_ReturnsOkWithRideActionResponse() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        doNothing().when(rideAcceptanceService).acceptRide(driverId, rideId);

        // Act
        ResponseEntity<RideActionResponse> response = driverController.acceptRide(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverId, response.getBody().getDriverId());
        assertEquals(rideId, response.getBody().getRideId());
        assertEquals("Driver accepted the ride successfully", response.getBody().getMessage());
        verify(rideAcceptanceService, times(1)).acceptRide(driverId, rideId);
    }

    @Test
    void testStartRide_WhenValidRequest_ReturnsOkWithRideActionResponse() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        doNothing().when(rideAcceptanceService).startRide(driverId, rideId);

        // Act
        ResponseEntity<RideActionResponse> response = driverController.startRide(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverId, response.getBody().getDriverId());
        assertEquals(rideId, response.getBody().getRideId());
        assertEquals("Driver started the ride successfully", response.getBody().getMessage());
        verify(rideAcceptanceService, times(1)).startRide(driverId, rideId);
    }

    @Test
    void testCompleteRide_WhenValidRequest_ReturnsOkWithRideCompletionResponse() {
        // Arrange
        Long driverId = 1L;
        doNothing().when(rideAcceptanceService).completeRide(eq(driverId), any(CompleteRideRequest.class));

        // Act
        ResponseEntity<RideCompletionResponse> response = driverController.completeRide(driverId, completeRideRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(driverId, response.getBody().getDriverId());
        assertEquals(completeRideRequest.getRideId(), response.getBody().getRideId());
        assertEquals(completeRideRequest.getActualDistanceKm(), response.getBody().getActualDistanceKm());
        assertEquals(completeRideRequest.getActualFare(), response.getBody().getActualFare());
        assertEquals("Ride completed successfully", response.getBody().getMessage());
        verify(rideAcceptanceService, times(1)).completeRide(driverId, completeRideRequest);
    }

    @Test
    void testNotifyDriver_WhenValidRequest_ReturnsOk() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        // Act
        ResponseEntity<Void> response = driverController.notifyDriver(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testNotifyDriverInternal_WhenValidRequest_ReturnsOk() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        // Act
        ResponseEntity<Void> response = driverController.notifyDriverInternal(driverId, rideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
