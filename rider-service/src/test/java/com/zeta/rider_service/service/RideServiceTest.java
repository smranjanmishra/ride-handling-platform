package com.zeta.rider_service.service;

import com.zeta.rider_service.client.DriverServiceClient;
import com.zeta.rider_service.client.NotificationServiceClient;
import com.zeta.rider_service.dto.*;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.repository.RideRepository;
import com.zeta.rider_service.statemachine.RideStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RiderService riderService;

    @Mock
    private RideValidationService rideValidationService;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private RideStateMachine rideStateMachine;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private RideService rideService;

    private CreateRideRequest createRideRequest;
    private Ride testRide;
    private DriverDTO driverDTO;

    @BeforeEach
    void setUp() {
        // Set field values using reflection
        ReflectionTestUtils.setField(rideService, "baseFare", 50.0);
        ReflectionTestUtils.setField(rideService, "perKmRate", 10.0);
        ReflectionTestUtils.setField(rideService, "searchRadiusKm", 10.0);

        createRideRequest = CreateRideRequest.builder()
                .riderId(1L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .pickupAddress("123 Main St")
                .dropLatitude(40.7589)
                .dropLongitude(-73.9851)
                .dropAddress("456 Park Ave")
                .build();

        driverDTO = DriverDTO.builder()
                .driverId(2L)
                .name("Driver Name")
                .vehicleNumber("ABC123")
                .vehicleType("Sedan")
                .latitude(40.7150)
                .longitude(-74.0050)
                .available(true)
                .build();

        testRide = Ride.builder()
                .rideId(100L)
                .riderId(1L)
                .driverId(2L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .dropLatitude(40.7589)
                .dropLongitude(-73.9851)
                .status(RideStatus.REQUESTED)
                .fareAmount(BigDecimal.valueOf(60.0))
                .distanceKm(1.0)
                .requestedAt(LocalDateTime.now())
                .assignedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateRide_Success_ShouldReturnRideDTO() {
        // Arrange
        doNothing().when(authorizationService).verifyRiderOwnership(1L);
        doNothing().when(rideValidationService).validateCreateRideRequest(any());
        when(rideRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(driverServiceClient.getAvailableDrivers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(driverDTO));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            ride.setRideId(100L);
            return ride;
        });
        doNothing().when(driverServiceClient).notifyDriver(anyLong(), anyLong());
        doNothing().when(notificationServiceClient).sendRideAccepted(anyLong(), anyString());
        doNothing().when(metricsService).incrementRidesCreated();
        doNothing().when(metricsService).recordRideCreationTime(anyLong());

        // Act
        RideDTO result = rideService.createRide(createRideRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        assertEquals(1L, result.getRiderId());
        assertEquals(2L, result.getDriverId());
        assertEquals(RideStatus.REQUESTED, result.getStatus());
        assertNotNull(result.getFareAmount());
        assertNotNull(result.getDistanceKm());

        verify(authorizationService).verifyRiderOwnership(1L);
        verify(rideValidationService).validateCreateRideRequest(createRideRequest);
        verify(driverServiceClient).getAvailableDrivers(40.7128, -74.0060, 10.0);
        verify(rideRepository).save(any(Ride.class));
        verify(driverServiceClient).notifyDriver(2L, 100L);
        verify(notificationServiceClient).sendRideAccepted(100L, "Driver Name");
        verify(metricsService).incrementRidesCreated();
        verify(metricsService).recordRideCreationTime(anyLong());
    }

    @Test
    void testCreateRide_NoDriversAvailable_ShouldThrowException() {
        // Arrange
        doNothing().when(authorizationService).verifyRiderOwnership(1L);
        doNothing().when(rideValidationService).validateCreateRideRequest(any());
        when(rideRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(driverServiceClient.getAvailableDrivers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());
        doNothing().when(metricsService).incrementAssignmentsFailed();

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> rideService.createRide(createRideRequest)
        );
        assertEquals("No drivers available nearby", exception.getMessage());

        verify(metricsService).incrementAssignmentsFailed();
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testCreateRide_DuplicateIdempotencyKey_ShouldReturnExistingRide() {
        // Arrange
        doNothing().when(authorizationService).verifyRiderOwnership(1L);
        doNothing().when(rideValidationService).validateCreateRideRequest(any());
        String idempotencyKey = "test-key";
        createRideRequest.setIdempotencyKey(idempotencyKey);
        when(rideRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);
        when(rideRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(testRide);
        doNothing().when(metricsService).recordRideCreationTime(anyLong());

        // Act
        RideDTO result = rideService.createRide(createRideRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        verify(rideRepository).existsByIdempotencyKey(idempotencyKey);
        verify(rideRepository).findByIdempotencyKey(idempotencyKey);
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverServiceClient, never()).getAvailableDrivers(anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testGetRide_Success_ShouldReturnRideDTO() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        doNothing().when(authorizationService).verifyRideAccess(testRide);

        // Act
        RideDTO result = rideService.getRide(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        verify(rideRepository).findById(100L);
        verify(authorizationService).verifyRideAccess(testRide);
    }

    @Test
    void testGetRide_NotFound_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRide(999L)
        );
        assertEquals("Ride not found: 999", exception.getMessage());

        verify(rideRepository).findById(999L);
        verify(authorizationService, never()).verifyRideAccess(any());
    }

    @Test
    void testCancelRide_Success_ShouldReturnCancelledRideDTO() {
        // Arrange
        CancelRideRequest cancelRequest = CancelRideRequest.builder()
                .cancellationReason("Changed my mind")
                .build();

        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        doNothing().when(authorizationService).verifyRideAccess(testRide);
        doNothing().when(rideValidationService).validateCancellation(testRide);
        doNothing().when(rideStateMachine).validateTransition(RideStatus.REQUESTED, RideStatus.CANCELLED);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            assertEquals(RideStatus.CANCELLED, ride.getStatus());
            assertEquals("Changed my mind", ride.getCancellationReason());
            assertNotNull(ride.getCancelledAt());
            return ride;
        });
        doNothing().when(metricsService).incrementRidesCancelled();

        // Act
        RideDTO result = rideService.cancelRide(100L, cancelRequest);

        // Assert
        assertNotNull(result);
        assertEquals(RideStatus.CANCELLED, result.getStatus());
        assertEquals("Changed my mind", result.getCancellationReason());
        assertNotNull(result.getCancelledAt());

        verify(rideRepository).save(any(Ride.class));
        verify(metricsService).incrementRidesCancelled();
    }

    @Test
    void testUpdateRideStatus_ToStarted_ShouldUpdateRide() {
        // Arrange
        UpdateRideStatusRequest updateRequest = UpdateRideStatusRequest.builder()
                .status(RideStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();

        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(authorizationService.isDriver()).thenReturn(false);
        when(rideStateMachine.validateAndGetNextStatus(RideStatus.REQUESTED, RideStatus.STARTED))
                .thenReturn(RideStatus.STARTED);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            assertEquals(RideStatus.STARTED, ride.getStatus());
            assertNotNull(ride.getStartedAt());
            return ride;
        });

        // Act
        RideDTO result = rideService.updateRideStatus(100L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(RideStatus.STARTED, result.getStatus());
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void testUpdateRideStatus_ToCompleted_ShouldUpdateRideAndIncrementRides() {
        // Arrange
        UpdateRideStatusRequest updateRequest = UpdateRideStatusRequest.builder()
                .status(RideStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        testRide.setStatus(RideStatus.STARTED);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(authorizationService.isDriver()).thenReturn(false);
        when(rideStateMachine.validateAndGetNextStatus(RideStatus.STARTED, RideStatus.COMPLETED))
                .thenReturn(RideStatus.COMPLETED);
        doNothing().when(riderService).incrementTotalRides(1L);
        doNothing().when(metricsService).incrementRidesCompleted();
        doNothing().when(notificationServiceClient).sendRideCompleted(100L);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            assertEquals(RideStatus.COMPLETED, ride.getStatus());
            assertNotNull(ride.getCompletedAt());
            return ride;
        });

        // Act
        RideDTO result = rideService.updateRideStatus(100L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(RideStatus.COMPLETED, result.getStatus());
        verify(riderService).incrementTotalRides(1L);
        verify(metricsService).incrementRidesCompleted();
        verify(notificationServiceClient).sendRideCompleted(100L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void testUpdateRideStatus_DriverNotAuthorized_ShouldThrowException() {
        // Arrange
        UpdateRideStatusRequest updateRequest = UpdateRideStatusRequest.builder()
                .status(RideStatus.STARTED)
                .build();

        testRide.setDriverId(2L);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(authorizationService.isDriver()).thenReturn(true);
        when(authorizationService.getCurrentUserId()).thenReturn(999L); // Different driver

        // Act & Assert
        org.springframework.security.access.AccessDeniedException exception = assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> rideService.updateRideStatus(100L, updateRequest)
        );
        assertEquals("You can only update rides assigned to you", exception.getMessage());

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testGetRidesByRider_Success_ShouldReturnList() {
        // Arrange
        List<Ride> rides = Arrays.asList(testRide);
        doNothing().when(authorizationService).verifyRiderOwnership(1L);
        when(rideRepository.findByRiderIdOrderByRequestedAtDesc(1L)).thenReturn(rides);

        // Act
        List<RideDTO> result = rideService.getRidesByRider(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getRideId());
        verify(authorizationService).verifyRiderOwnership(1L);
        verify(rideRepository).findByRiderIdOrderByRequestedAtDesc(1L);
    }

    @Test
    void testGetNearbyCabs_Success_ShouldReturnDriverList() {
        // Arrange
        List<DriverDTO> drivers = Arrays.asList(driverDTO);
        when(driverServiceClient.getAvailableDrivers(40.7128, -74.0060, 5.0))
                .thenReturn(drivers);

        // Act
        List<DriverDTO> result = rideService.getNearbyCabs(40.7128, -74.0060, 5.0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getDriverId());
        verify(driverServiceClient).getAvailableDrivers(40.7128, -74.0060, 5.0);
    }

    @Test
    void testGetRideEntity_Success_ShouldReturnRide() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));

        // Act
        Ride result = rideService.getRideEntity(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getRideId());
        verify(rideRepository).findById(100L);
    }

    @Test
    void testGetRideEntity_NotFound_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> rideService.getRideEntity(999L)
        );
        assertEquals("Ride not found: 999", exception.getMessage());
    }

    @Test
    void testCreateRide_CalculatesFareCorrectly() {
        // Arrange
        doNothing().when(authorizationService).verifyRiderOwnership(1L);
        doNothing().when(rideValidationService).validateCreateRideRequest(any());
        when(rideRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(driverServiceClient.getAvailableDrivers(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(driverDTO));
        
        // Capture the ride to verify calculations
        Ride[] capturedRide = new Ride[1];
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            capturedRide[0] = ride;
            ride.setRideId(100L);
            return ride;
        });
        doNothing().when(driverServiceClient).notifyDriver(anyLong(), anyLong());
        doNothing().when(notificationServiceClient).sendRideAccepted(anyLong(), anyString());
        doNothing().when(metricsService).incrementRidesCreated();
        doNothing().when(metricsService).recordRideCreationTime(anyLong());

        // Act
        rideService.createRide(createRideRequest);

        // Assert - Verify fare and distance calculations
        assertNotNull(capturedRide[0], "Ride should be saved");
        assertNotNull(capturedRide[0].getFareAmount(), "Fare amount should be calculated");
        assertNotNull(capturedRide[0].getDistanceKm(), "Distance should be calculated");
        
        // Calculate expected values
        // Distance: sqrt((latDiff * 111)^2 + (lonDiff * 111)^2)
        // latDiff = 40.7589 - 40.7128 = 0.0461
        // lonDiff = -73.9851 - (-74.0060) = 0.0209
        // distance ≈ sqrt((0.0461 * 111)^2 + (0.0209 * 111)^2) ≈ 5.62 km
        double expectedDistance = Math.sqrt(
            Math.pow((40.7589 - 40.7128) * 111, 2) + 
            Math.pow((-73.9851 - (-74.0060)) * 111, 2)
        );
        
        // Fare: baseFare (50) + distance * perKmRate (10)
        double expectedFare = 50.0 + (expectedDistance * 10.0);
        
        // Allow small tolerance for floating point calculations
        assertEquals(expectedDistance, capturedRide[0].getDistanceKm(), 0.1, 
            "Distance should be calculated correctly");
        assertEquals(expectedFare, capturedRide[0].getFareAmount().doubleValue(), 1.0, 
            "Fare should be calculated correctly (baseFare + distance * perKmRate)");
        
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void testUpdateRideStatus_ToStarted_WithoutStartedAt_ShouldUseCurrentTime() {
        // Arrange
        UpdateRideStatusRequest updateRequest = UpdateRideStatusRequest.builder()
                .status(RideStatus.STARTED)
                .startedAt(null) // Not provided
                .build();

        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(authorizationService.isDriver()).thenReturn(false);
        when(rideStateMachine.validateAndGetNextStatus(RideStatus.REQUESTED, RideStatus.STARTED))
                .thenReturn(RideStatus.STARTED);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            assertEquals(RideStatus.STARTED, ride.getStatus());
            assertNotNull(ride.getStartedAt()); // Should be set to current time
            return ride;
        });

        // Act
        rideService.updateRideStatus(100L, updateRequest);

        // Assert
        verify(rideRepository).save(any(Ride.class));
    }
}
