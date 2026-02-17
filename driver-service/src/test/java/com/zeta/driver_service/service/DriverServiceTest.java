package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.CreateDriverRequest;
import com.zeta.driver_service.dto.DriverDTO;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.entity.DriverAvailability;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.enums.DriverStatus;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.exception.DuplicateEmailException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
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
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverLocationRepository driverLocationRepository;

    @Mock
    private DriverAvailabilityRepository driverAvailabilityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DriverService driverService;

    private CreateDriverRequest createDriverRequest;
    private Driver savedDriver;
    private DriverAvailability driverAvailability;

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

        savedDriver = Driver.builder()
                .driverId(1L)
                .name("Test Driver")
                .email("driver@example.com")
                .phone("1234567890")
                .passwordHash("encodedPassword")
                .licenseNumber("LIC123")
                .vehicleNumber("VEH123")
                .vehicleType("SEDAN")
                .status(DriverStatus.ACTIVE)
                .totalRides(0)
                .build();

        driverAvailability = DriverAvailability.builder()
                .id(1L)
                .driverId(1L)
                .available(false)
                .build();
    }

    @Test
    void testCreateDriver_WhenValidRequest_CreatesDriver() {
        // Arrange
        when(driverRepository.existsByEmail(createDriverRequest.getEmail())).thenReturn(false);
        when(driverRepository.existsByPhone(createDriverRequest.getPhone())).thenReturn(false);
        when(driverRepository.existsByLicenseNumber(createDriverRequest.getLicenseNumber())).thenReturn(false);
        when(passwordEncoder.encode(createDriverRequest.getPassword())).thenReturn("encodedPassword");
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);
        when(driverAvailabilityRepository.save(any(DriverAvailability.class))).thenReturn(driverAvailability);

        // Act
        DriverDTO result = driverService.createDriver(createDriverRequest);

        // Assert
        assertNotNull(result);
        assertEquals(savedDriver.getDriverId(), result.getDriverId());
        assertEquals(savedDriver.getName(), result.getName());
        assertEquals(savedDriver.getEmail(), result.getEmail());
        verify(driverRepository).existsByEmail(createDriverRequest.getEmail());
        verify(driverRepository).existsByPhone(createDriverRequest.getPhone());
        verify(driverRepository).existsByLicenseNumber(createDriverRequest.getLicenseNumber());
        verify(passwordEncoder).encode(createDriverRequest.getPassword());
        verify(driverRepository).save(any(Driver.class));
        verify(driverAvailabilityRepository).save(any(DriverAvailability.class));
    }

    @Test
    void testCreateDriver_WhenEmailExists_ThrowsDuplicateEmailException() {
        // Arrange
        when(driverRepository.existsByEmail(createDriverRequest.getEmail())).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> driverService.createDriver(createDriverRequest));
        assertEquals("Email already registered", exception.getMessage());
        verify(driverRepository).existsByEmail(createDriverRequest.getEmail());
        verify(driverRepository, never()).existsByPhone(any());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testCreateDriver_WhenPhoneExists_ThrowsDuplicateEmailException() {
        // Arrange
        when(driverRepository.existsByEmail(createDriverRequest.getEmail())).thenReturn(false);
        when(driverRepository.existsByPhone(createDriverRequest.getPhone())).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> driverService.createDriver(createDriverRequest));
        assertEquals("Phone already registered", exception.getMessage());
        verify(driverRepository).existsByEmail(createDriverRequest.getEmail());
        verify(driverRepository).existsByPhone(createDriverRequest.getPhone());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testCreateDriver_WhenLicenseNumberExists_ThrowsDuplicateEmailException() {
        // Arrange
        when(driverRepository.existsByEmail(createDriverRequest.getEmail())).thenReturn(false);
        when(driverRepository.existsByPhone(createDriverRequest.getPhone())).thenReturn(false);
        when(driverRepository.existsByLicenseNumber(createDriverRequest.getLicenseNumber())).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> driverService.createDriver(createDriverRequest));
        assertEquals("License number already registered", exception.getMessage());
        verify(driverRepository).existsByEmail(createDriverRequest.getEmail());
        verify(driverRepository).existsByPhone(createDriverRequest.getPhone());
        verify(driverRepository).existsByLicenseNumber(createDriverRequest.getLicenseNumber());
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testGetDriver_WhenDriverExists_ReturnsDriverDTO() {
        // Arrange
        Long driverId = 1L;
        DriverLocation location = DriverLocation.builder()
                .driverId(driverId)
                .latitude(40.7128)
                .longitude(-74.0060)
                .build();

        DriverAvailability availability = DriverAvailability.builder()
                .driverId(driverId)
                .available(true)
                .build();

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.of(location));
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(availability));

        // Act
        DriverDTO result = driverService.getDriver(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(savedDriver.getDriverId(), result.getDriverId());
        assertEquals(location.getLatitude(), result.getLatitude());
        assertEquals(location.getLongitude(), result.getLongitude());
        assertTrue(result.getAvailable());
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository).findByDriverId(driverId);
    }

    @Test
    void testGetDriver_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverService.getDriver(driverId));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository, never()).findByDriverId(any());
    }

    @Test
    void testGetDriver_WhenNoLocation_ReturnsDriverDTOWithNullLocation() {
        // Arrange
        Long driverId = 1L;
        DriverAvailability availability = DriverAvailability.builder()
                .driverId(driverId)
                .available(false)
                .build();

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.empty());
        when(driverAvailabilityRepository.findByDriverId(driverId)).thenReturn(Optional.of(availability));

        // Act
        DriverDTO result = driverService.getDriver(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(savedDriver.getDriverId(), result.getDriverId());
        assertNull(result.getLatitude());
        assertNull(result.getLongitude());
        assertFalse(result.getAvailable());
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverAvailabilityRepository).findByDriverId(driverId);
    }

    @Test
    void testIncrementTotalRides_WhenDriverExists_IncrementsRides() {
        // Arrange
        Long driverId = 1L;
        savedDriver.setTotalRides(5);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        driverService.incrementTotalRides(driverId);

        // Assert
        assertEquals(6, savedDriver.getTotalRides());
        verify(driverRepository).findById(driverId);
        verify(driverRepository).save(savedDriver);
    }

    @Test
    void testIncrementTotalRides_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverService.incrementTotalRides(driverId));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(driverRepository).findById(driverId);
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testUpdateLocation_WhenLocationExists_UpdatesLocation() {
        // Arrange
        Long driverId = 1L;
        double latitude = 40.7128;
        double longitude = -74.0060;

        DriverLocation existingLocation = DriverLocation.builder()
                .locationId(1L)
                .driverId(driverId)
                .latitude(40.7000)
                .longitude(-74.0000)
                .build();

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.of(existingLocation));
        when(driverLocationRepository.save(any(DriverLocation.class))).thenReturn(existingLocation);

        // Act
        Driver result = driverService.updateLocation(driverId, latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals(savedDriver, result);
        assertEquals(latitude, existingLocation.getLatitude());
        assertEquals(longitude, existingLocation.getLongitude());
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverLocationRepository).save(existingLocation);
    }

    @Test
    void testUpdateLocation_WhenLocationDoesNotExist_CreatesNewLocation() {
        // Arrange
        Long driverId = 1L;
        double latitude = 40.7128;
        double longitude = -74.0060;

        DriverLocation newLocation = DriverLocation.builder()
                .driverId(driverId)
                .latitude(latitude)
                .longitude(longitude)
                .build();

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(savedDriver));
        when(driverLocationRepository.findByDriverId(driverId)).thenReturn(Optional.empty());
        when(driverLocationRepository.save(any(DriverLocation.class))).thenReturn(newLocation);

        // Act
        Driver result = driverService.updateLocation(driverId, latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals(savedDriver, result);
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository).findByDriverId(driverId);
        verify(driverLocationRepository).save(any(DriverLocation.class));
    }

    @Test
    void testUpdateLocation_WhenDriverNotFound_ThrowsDriverNotFoundException() {
        // Arrange
        Long driverId = 1L;
        double latitude = 40.7128;
        double longitude = -74.0060;
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        DriverNotFoundException exception = assertThrows(DriverNotFoundException.class,
                () -> driverService.updateLocation(driverId, latitude, longitude));
        assertEquals("Driver not found: 1", exception.getMessage());
        verify(driverRepository).findById(driverId);
        verify(driverLocationRepository, never()).findByDriverId(any());
    }
}
