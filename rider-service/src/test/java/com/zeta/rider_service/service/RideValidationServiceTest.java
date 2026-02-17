package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.CreateRideRequest;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.InvalidRideRequestException;
import com.zeta.rider_service.exception.InvalidRideStateException;
import com.zeta.rider_service.exception.OngoingRideExistsException;
import com.zeta.rider_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideValidationServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideValidationService rideValidationService;

    private CreateRideRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CreateRideRequest.builder()
                .riderId(1L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .pickupAddress("123 Main St")
                .dropLatitude(40.7589)
                .dropLongitude(-73.9851)
                .dropAddress("456 Park Ave")
                .build();
    }

    @Test
    void testValidateCreateRideRequest_ValidRequest_ShouldSucceed() {
        // Arrange
        when(rideRepository.existsByRiderIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> rideValidationService.validateCreateRideRequest(validRequest));
        verify(rideRepository).existsByRiderIdAndStatusIn(eq(1L), anyList());
    }

    @Test
    void testValidateCreateRideRequest_SamePickupAndDropLocation_ShouldThrowException() {
        // Arrange
        CreateRideRequest invalidRequest = CreateRideRequest.builder()
                .riderId(1L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .dropLatitude(40.7128)  // Same as pickup
                .dropLongitude(-74.0060) // Same as pickup
                .build();

        // Act & Assert
        InvalidRideRequestException exception = assertThrows(
                InvalidRideRequestException.class,
                () -> rideValidationService.validateCreateRideRequest(invalidRequest)
        );
        assertEquals("Pickup and drop locations cannot be the same", exception.getMessage());
        verify(rideRepository, never()).existsByRiderIdAndStatusIn(anyLong(), anyList());
    }

    @Test
    void testValidateCreateRideRequest_OngoingRideExists_ShouldThrowException() {
        // Arrange
        when(rideRepository.existsByRiderIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(true);

        // Act & Assert
        OngoingRideExistsException exception = assertThrows(
                OngoingRideExistsException.class,
                () -> rideValidationService.validateCreateRideRequest(validRequest)
        );
        assertEquals("You already have an ongoing ride", exception.getMessage());
        verify(rideRepository).existsByRiderIdAndStatusIn(eq(1L), anyList());
    }

    @Test
    void testValidateCancellation_RequestedStatus_ShouldSucceed() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.REQUESTED)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> rideValidationService.validateCancellation(ride));
    }

    @Test
    void testValidateCancellation_DriverAssignedStatus_ShouldSucceed() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.DRIVER_ASSIGNED)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> rideValidationService.validateCancellation(ride));
    }

    @Test
    void testValidateCancellation_DriverArrivedStatus_ShouldSucceed() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.DRIVER_ARRIVED)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> rideValidationService.validateCancellation(ride));
    }

    @Test
    void testValidateCancellation_CompletedStatus_ShouldThrowException() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.COMPLETED)
                .build();

        // Act & Assert
        InvalidRideStateException exception = assertThrows(
                InvalidRideStateException.class,
                () -> rideValidationService.validateCancellation(ride)
        );
        assertEquals("Cannot cancel a completed ride", exception.getMessage());
    }

    @Test
    void testValidateCancellation_AlreadyCancelled_ShouldThrowException() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.CANCELLED)
                .build();

        // Act & Assert
        InvalidRideStateException exception = assertThrows(
                InvalidRideStateException.class,
                () -> rideValidationService.validateCancellation(ride)
        );
        assertEquals("Ride is already cancelled", exception.getMessage());
    }

    @Test
    void testValidateCancellation_StartedStatus_ShouldThrowException() {
        // Arrange
        Ride ride = Ride.builder()
                .rideId(100L)
                .status(RideStatus.STARTED)
                .build();

        // Act & Assert
        InvalidRideStateException exception = assertThrows(
                InvalidRideStateException.class,
                () -> rideValidationService.validateCancellation(ride)
        );
        assertEquals("Cannot cancel a ride that has already started", exception.getMessage());
    }

    @Test
    void testValidateCreateRideRequest_ChecksOngoingStatuses() {
        // Arrange
        when(rideRepository.existsByRiderIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(false);

        // Act
        rideValidationService.validateCreateRideRequest(validRequest);

        // Assert - Verify that the correct statuses are checked
        verify(rideRepository).existsByRiderIdAndStatusIn(
                eq(1L),
                argThat(statusList -> {
                    List<RideStatus> expectedStatuses = Arrays.asList(
                            RideStatus.REQUESTED,
                            RideStatus.DRIVER_ASSIGNED,
                            RideStatus.DRIVER_ARRIVED,
                            RideStatus.STARTED
                    );
                    return statusList.size() == expectedStatuses.size() &&
                            statusList.containsAll(expectedStatuses);
                })
        );
    }
}
