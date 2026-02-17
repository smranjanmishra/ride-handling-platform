package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.PaymentDTO;
import com.zeta.rider_service.dto.ProcessPaymentRequest;
import com.zeta.rider_service.entity.Payment;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.PaymentMethod;
import com.zeta.rider_service.enums.PaymentStatus;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.PaymentNotFoundException;
import com.zeta.rider_service.exception.PaymentProcessingException;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.repository.PaymentRepository;
import com.zeta.rider_service.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Ride testRide;
    private ProcessPaymentRequest processPaymentRequest;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        testRide = Ride.builder()
                .rideId(100L)
                .riderId(1L)
                .driverId(2L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .dropLatitude(40.7589)
                .dropLongitude(-73.9851)
                .status(RideStatus.COMPLETED)
                .fareAmount(BigDecimal.valueOf(100.50))
                .distanceKm(5.0)
                .build();

        processPaymentRequest = ProcessPaymentRequest.builder()
                .rideId(100L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        savedPayment = Payment.builder()
                .paymentId(200L)
                .rideId(100L)
                .riderId(1L)
                .amount(BigDecimal.valueOf(100.50))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("txn-12345")
                .paymentDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testProcessPayment_Success_ShouldReturnPaymentDTO() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(200L);
            payment.setTransactionId("txn-12345");
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        });

        // Act
        PaymentDTO result = paymentService.processPayment(processPaymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getPaymentId());
        assertEquals(100L, result.getRideId());
        assertEquals(1L, result.getRiderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getPaymentDate());

        verify(rideRepository).findById(100L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_RideNotFound_ShouldThrowException() {
        // Arrange
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());
        processPaymentRequest.setRideId(999L);

        // Act & Assert
        RideNotFoundException exception = assertThrows(
                RideNotFoundException.class,
                () -> paymentService.processPayment(processPaymentRequest)
        );
        assertEquals("Ride not found: 999", exception.getMessage());

        verify(rideRepository).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_PaymentProcessingFailure_ShouldSaveFailedPaymentAndThrowException() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        // First save throws exception, second save (in catch block) succeeds
        doThrow(new RuntimeException("Database error"))
                .doAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);
                    // Verify that status is set to FAILED in catch block
                    assertEquals(PaymentStatus.FAILED, payment.getStatus());
                    payment.setPaymentId(200L);
                    return payment;
                })
                .when(paymentRepository).save(any(Payment.class));

        // Act & Assert
        PaymentProcessingException exception = assertThrows(
                PaymentProcessingException.class,
                () -> paymentService.processPayment(processPaymentRequest)
        );
        assertTrue(exception.getMessage().contains("Payment failed"));
        assertTrue(exception.getMessage().contains("Database error"));

        // Verify that payment was saved twice: once initially (throws), and once with FAILED status
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testProcessPayment_SetsCorrectInitialStatus() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        Payment[] capturedPayment = new Payment[1];
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            capturedPayment[0] = payment;
            payment.setPaymentId(200L);
            payment.setTransactionId("txn-12345");
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        });

        // Act
        paymentService.processPayment(processPaymentRequest);

        // Assert - Verify payment was created with correct initial values
        assertNotNull(capturedPayment[0]);
        assertEquals(100L, capturedPayment[0].getRideId());
        assertEquals(1L, capturedPayment[0].getRiderId());
        assertEquals(BigDecimal.valueOf(100.50), capturedPayment[0].getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, capturedPayment[0].getPaymentMethod());
        // Status should be COMPLETED after successful save
        assertEquals(PaymentStatus.COMPLETED, capturedPayment[0].getStatus());
        assertNotNull(capturedPayment[0].getTransactionId());
        assertNotNull(capturedPayment[0].getPaymentDate());
    }

    @Test
    void testProcessPayment_WithDifferentPaymentMethods() {
        // Arrange
        PaymentMethod[] methods = {PaymentMethod.CASH, PaymentMethod.UPI, PaymentMethod.WALLET, PaymentMethod.DEBIT_CARD};
        
        for (PaymentMethod method : methods) {
            processPaymentRequest.setPaymentMethod(method);
            when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                payment.setPaymentId(200L);
                payment.setTransactionId("txn-12345");
                payment.setPaymentDate(LocalDateTime.now());
                return payment;
            });

            // Act
            PaymentDTO result = paymentService.processPayment(processPaymentRequest);

            // Assert
            assertEquals(method, result.getPaymentMethod());
            // Clear invocations for next iteration
            clearInvocations(rideRepository);
            clearInvocations(paymentRepository);
        }
    }

    @Test
    void testGetPaymentByRide_Success_ShouldReturnPaymentDTO() {
        // Arrange
        when(paymentRepository.findByRideId(100L)).thenReturn(Optional.of(savedPayment));

        // Act
        PaymentDTO result = paymentService.getPaymentByRide(100L);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getPaymentId());
        assertEquals(100L, result.getRideId());
        assertEquals(1L, result.getRiderId());
        assertEquals(BigDecimal.valueOf(100.50), result.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());

        verify(paymentRepository).findByRideId(100L);
    }

    @Test
    void testGetPaymentByRide_NotFound_ShouldThrowException() {
        // Arrange
        when(paymentRepository.findByRideId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentByRide(999L)
        );
        assertEquals("Payment not found for ride: 999", exception.getMessage());

        verify(paymentRepository).findByRideId(999L);
    }

    @Test
    void testGetPaymentsByRider_Success_ShouldReturnList() {
        // Arrange
        Payment payment1 = Payment.builder()
                .paymentId(200L)
                .rideId(100L)
                .riderId(1L)
                .amount(BigDecimal.valueOf(100.50))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment payment2 = Payment.builder()
                .paymentId(201L)
                .rideId(101L)
                .riderId(1L)
                .amount(BigDecimal.valueOf(75.25))
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.COMPLETED)
                .build();

        List<Payment> payments = Arrays.asList(payment1, payment2);
        when(paymentRepository.findByRiderId(1L)).thenReturn(payments);

        // Act
        List<PaymentDTO> result = paymentService.getPaymentsByRider(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(200L, result.get(0).getPaymentId());
        assertEquals(201L, result.get(1).getPaymentId());
        assertEquals(1L, result.get(0).getRiderId());
        assertEquals(1L, result.get(1).getRiderId());

        verify(paymentRepository).findByRiderId(1L);
    }

    @Test
    void testGetPaymentsByRider_EmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(paymentRepository.findByRiderId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<PaymentDTO> result = paymentService.getPaymentsByRider(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(paymentRepository).findByRiderId(1L);
    }

    @Test
    void testProcessPayment_GeneratesTransactionId() {
        // Arrange
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(200L);
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        });

        // Act
        PaymentDTO result = paymentService.processPayment(processPaymentRequest);

        // Assert
        assertNotNull(result.getTransactionId());
        assertFalse(result.getTransactionId().isEmpty());
        // UUID format check (basic validation)
        assertTrue(result.getTransactionId().length() > 0);
    }

    @Test
    void testProcessPayment_SetsPaymentDate() {
        // Arrange
        LocalDateTime beforeCall = LocalDateTime.now();
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(200L);
            payment.setTransactionId("txn-12345");
            return payment;
        });

        // Act
        PaymentDTO result = paymentService.processPayment(processPaymentRequest);
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        assertNotNull(result.getPaymentDate());
        assertTrue(result.getPaymentDate().isAfter(beforeCall.minusSeconds(1)) || 
                   result.getPaymentDate().isEqual(beforeCall));
        assertTrue(result.getPaymentDate().isBefore(afterCall.plusSeconds(1)) || 
                   result.getPaymentDate().isEqual(afterCall));
    }

    @Test
    void testProcessPayment_UsesRideFareAmount() {
        // Arrange
        BigDecimal customFare = BigDecimal.valueOf(250.75);
        testRide.setFareAmount(customFare);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        Payment[] capturedPayment = new Payment[1];
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            capturedPayment[0] = payment;
            payment.setPaymentId(200L);
            payment.setTransactionId("txn-12345");
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        });

        // Act
        PaymentDTO result = paymentService.processPayment(processPaymentRequest);

        // Assert
        assertEquals(customFare, result.getAmount());
        assertEquals(customFare, capturedPayment[0].getAmount());
    }

    @Test
    void testProcessPayment_UsesRideRiderId() {
        // Arrange
        Long customRiderId = 999L;
        testRide.setRiderId(customRiderId);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(testRide));
        Payment[] capturedPayment = new Payment[1];
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            capturedPayment[0] = payment;
            payment.setPaymentId(200L);
            payment.setTransactionId("txn-12345");
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        });

        // Act
        PaymentDTO result = paymentService.processPayment(processPaymentRequest);

        // Assert
        assertEquals(customRiderId, result.getRiderId());
        assertEquals(customRiderId, capturedPayment[0].getRiderId());
    }
}
