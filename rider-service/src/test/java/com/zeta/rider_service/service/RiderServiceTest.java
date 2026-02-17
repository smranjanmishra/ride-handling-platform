package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.CreateRiderRequest;
import com.zeta.rider_service.dto.RiderDTO;
import com.zeta.rider_service.entity.Rider;
import com.zeta.rider_service.enums.RiderStatus;
import com.zeta.rider_service.exception.DuplicateEmailException;
import com.zeta.rider_service.exception.DuplicatePhoneException;
import com.zeta.rider_service.exception.RiderNotFoundException;
import com.zeta.rider_service.repository.RiderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RiderService riderService;

    private CreateRiderRequest createRiderRequest;
    private Rider savedRider;

    @BeforeEach
    void setUp() {
        createRiderRequest = CreateRiderRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        savedRider = Rider.builder()
                .riderId(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .passwordHash("encodedPassword")
                .status(RiderStatus.ACTIVE)
                .totalRides(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateRider_Success_ShouldReturnRiderDTO() {
        // Arrange
        when(riderRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(riderRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(riderRepository.save(any(Rider.class))).thenReturn(savedRider);

        // Act
        RiderDTO result = riderService.createRider(createRiderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRiderId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(0, result.getTotalRides());
        assertEquals(RiderStatus.ACTIVE, result.getStatus());

        verify(riderRepository).existsByEmail("john@example.com");
        verify(riderRepository).existsByPhone("1234567890");
        verify(passwordEncoder).encode("password123");
        verify(riderRepository).save(any(Rider.class));
    }

    @Test
    void testCreateRider_DuplicateEmail_ShouldThrowException() {
        // Arrange
        when(riderRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> riderService.createRider(createRiderRequest)
        );
        assertEquals("Email already registered", exception.getMessage());

        verify(riderRepository).existsByEmail("john@example.com");
        verify(riderRepository, never()).existsByPhone(anyString());
        verify(riderRepository, never()).save(any(Rider.class));
    }

    @Test
    void testCreateRider_DuplicatePhone_ShouldThrowException() {
        // Arrange
        when(riderRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(riderRepository.existsByPhone("1234567890")).thenReturn(true);

        // Act & Assert
        DuplicatePhoneException exception = assertThrows(
                DuplicatePhoneException.class,
                () -> riderService.createRider(createRiderRequest)
        );
        assertEquals("Phone already registered", exception.getMessage());

        verify(riderRepository).existsByEmail("john@example.com");
        verify(riderRepository).existsByPhone("1234567890");
        verify(riderRepository, never()).save(any(Rider.class));
    }

    @Test
    void testGetRider_Success_ShouldReturnRiderDTO() {
        // Arrange
        when(riderRepository.findById(1L)).thenReturn(Optional.of(savedRider));

        // Act
        RiderDTO result = riderService.getRider(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRiderId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(riderRepository).findById(1L);
    }

    @Test
    void testGetRider_NotFound_ShouldThrowException() {
        // Arrange
        when(riderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RiderNotFoundException exception = assertThrows(
                RiderNotFoundException.class,
                () -> riderService.getRider(999L)
        );
        assertEquals("Rider not found: 999", exception.getMessage());

        verify(riderRepository).findById(999L);
    }

    @Test
    void testIncrementTotalRides_Success_ShouldIncrementRides() {
        // Arrange
        Rider rider = Rider.builder()
                .riderId(1L)
                .totalRides(5)
                .build();
        when(riderRepository.findById(1L)).thenReturn(Optional.of(rider));
        when(riderRepository.save(any(Rider.class))).thenAnswer(invocation -> {
            Rider saved = invocation.getArgument(0);
            assertEquals(6, saved.getTotalRides());
            return saved;
        });

        // Act
        riderService.incrementTotalRides(1L);

        // Assert
        verify(riderRepository).findById(1L);
        verify(riderRepository).save(any(Rider.class));
    }

    @Test
    void testIncrementTotalRides_NotFound_ShouldThrowException() {
        // Arrange
        when(riderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RiderNotFoundException exception = assertThrows(
                RiderNotFoundException.class,
                () -> riderService.incrementTotalRides(999L)
        );
        assertEquals("Rider not found: 999", exception.getMessage());

        verify(riderRepository).findById(999L);
        verify(riderRepository, never()).save(any(Rider.class));
    }

    @Test
    void testCreateRider_SetsDefaultValues() {
        // Arrange
        when(riderRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(riderRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(riderRepository.save(any(Rider.class))).thenAnswer(invocation -> {
            Rider rider = invocation.getArgument(0);
            assertNull(rider.getRiderId()); // Not set before save
            assertEquals(RiderStatus.ACTIVE, rider.getStatus());
            assertEquals(0, rider.getTotalRides());
            return savedRider;
        });

        // Act
        riderService.createRider(createRiderRequest);

        // Assert
        verify(riderRepository).save(any(Rider.class));
    }
}
