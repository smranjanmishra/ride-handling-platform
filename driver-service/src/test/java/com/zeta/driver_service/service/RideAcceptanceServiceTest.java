package com.zeta.driver_service.service;

import com.zeta.driver_service.client.NotificationServiceClient;
import com.zeta.driver_service.client.RiderServiceClient;
import com.zeta.driver_service.dto.CompleteRideRequest;
import com.zeta.driver_service.dto.UpdateRideStatusRequest;
import com.zeta.driver_service.entity.DriverAvailability;
import com.zeta.driver_service.exception.DriverNotAvailableException;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideAcceptanceServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverAvailabilityRepository driverAvailabilityRepository;

    @Mock
    private DriverAvailabilityService driverAvailabilityService;

    @Mock
    private RiderServiceClient riderServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private RideAcceptanceService rideAcceptanceService;

    @Captor
    private ArgumentCaptor<UpdateRideStatusRequest> statusRequestCaptor;

    private DriverAvailability driverAvailability;
    private CompleteRideRequest completeRideRequest;

    @BeforeEach
    void setUp() {
        driverAvailability = DriverAvailability.builder()
                .id(1L)
                .driverId(1L)
                .available(true)
                .currentRideId(null)
                .build();

        completeRideRequest = CompleteRideRequest.builder()
                .rideId(100L)
                .actualDistanceKm(5.5)
                .actualFare(150.0)
                .build();
    }

    @Test
    void testAcceptRide_WhenDriverAvailable_AcceptsRide() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(true);
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(driverAvailability));
        doNothing().when(driverAvailabilityService).markDriverBusy(driverId, rideId);
        doNothing().when(riderServiceClient).updateRideStatus(eq(rideId), any(UpdateRideStatusRequest.class));
        doNothing().when(metricsService).incrementRidesAccepted();
        doNothing().when(metricsService).recordRideAcceptanceTime(anyLong());

        // Act
        rideAcceptanceService.acceptRide(driverId, rideId);

        // Assert
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(driverAvailabilityService).markDriverBusy(driverId, rideId);
        verify(riderServiceClient).updateRideStatus(eq(rideId), statusRequestCaptor.capture());
        UpdateRideStatusRequest capturedRequest = statusRequestCaptor.getValue();
        assertEquals("DRIVER_ASSIGNED", capturedRequest.getStatus());
        verify(metricsService).incrementRidesAccepted();
        verify(metricsService).recordRideAcceptanceTime(anyLong());
    }

    @Test
    void testAcceptRide_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(false);

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> rideAcceptanceService.acceptRide(driverId, rideId));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverAvailabilityRepository, never()).findByDriverId(any());
        verify(driverAvailabilityService, never()).markDriverBusy(any(), any());
        verify(metricsService, never()).incrementRidesAccepted();
    }

    @Test
    void testAcceptRide_WhenDriverNotAvailable_ThrowsDriverNotAvailableException() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        driverAvailability.setAvailable(false);

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(true);
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(driverAvailability));
        doNothing().when(metricsService).incrementRidesRejected();

        // Act & Assert
        DriverNotAvailableException exception = assertThrows(DriverNotAvailableException.class,
                () -> rideAcceptanceService.acceptRide(driverId, rideId));
        assertEquals("Driver is not available", exception.getMessage());
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(metricsService).incrementRidesRejected();
        verify(driverAvailabilityService, never()).markDriverBusy(any(), any());
        verify(metricsService, never()).incrementRidesAccepted();
    }

    @Test
    void testAcceptRide_WhenAvailabilityNotFound_ThrowsDriverNotAvailableException() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        when(driverRepository.existsById(driverId)).thenReturn(true);
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.empty());
        doNothing().when(metricsService).incrementRidesRejected();

        // Act & Assert
        DriverNotAvailableException exception = assertThrows(DriverNotAvailableException.class,
                () -> rideAcceptanceService.acceptRide(driverId, rideId));
        assertEquals("Driver is not available", exception.getMessage());
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(driverRepository).existsById(driverId);
        verify(driverAvailabilityRepository).findByDriverId(driverId);
        verify(metricsService).incrementRidesRejected();
        verify(driverAvailabilityService, never()).markDriverBusy(any(), any());
    }

    @Test
    void testStartRide_WhenValid_StartsRide() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        doNothing().when(riderServiceClient).updateRideStatus(eq(rideId), any(UpdateRideStatusRequest.class));

        // Act
        rideAcceptanceService.startRide(driverId, rideId);

        // Assert
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(riderServiceClient).updateRideStatus(eq(rideId), statusRequestCaptor.capture());
        UpdateRideStatusRequest capturedRequest = statusRequestCaptor.getValue();
        assertEquals("STARTED", capturedRequest.getStatus());
        assertNotNull(capturedRequest.getStartedAt());
    }

    @Test
    void testCompleteRide_WhenValid_CompletesRide() {
        // Arrange
        Long driverId = 1L;

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        doNothing().when(riderServiceClient).updateRideStatus(eq(completeRideRequest.getRideId()), any(UpdateRideStatusRequest.class));
        doNothing().when(driverAvailabilityService).markDriverAvailable(driverId);
        doNothing().when(metricsService).incrementRidesCompleted();

        // Act
        rideAcceptanceService.completeRide(driverId, completeRideRequest);

        // Assert
        verify(authorizationService).verifyDriverOwnership(driverId);
        verify(riderServiceClient).updateRideStatus(eq(completeRideRequest.getRideId()), statusRequestCaptor.capture());
        UpdateRideStatusRequest capturedRequest = statusRequestCaptor.getValue();
        assertEquals("COMPLETED", capturedRequest.getStatus());
        assertNotNull(capturedRequest.getCompletedAt());
        verify(driverAvailabilityService).markDriverAvailable(driverId);
        verify(metricsService).incrementRidesCompleted();
    }

    @Test
    void testCompleteRide_WhenValid_UpdatesRideStatusWithCorrectRideId() {
        // Arrange
        Long driverId = 1L;
        Long rideId = 100L;
        completeRideRequest.setRideId(rideId);

        doNothing().when(authorizationService).verifyDriverOwnership(driverId);
        doNothing().when(riderServiceClient).updateRideStatus(eq(rideId), any(UpdateRideStatusRequest.class));
        doNothing().when(driverAvailabilityService).markDriverAvailable(driverId);
        doNothing().when(metricsService).incrementRidesCompleted();

        // Act
        rideAcceptanceService.completeRide(driverId, completeRideRequest);

        // Assert
        verify(riderServiceClient).updateRideStatus(eq(rideId), any(UpdateRideStatusRequest.class));
    }
}
