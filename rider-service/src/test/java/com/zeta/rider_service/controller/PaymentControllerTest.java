package com.zeta.rider_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.rider_service.dto.PaymentDTO;
import com.zeta.rider_service.dto.ProcessPaymentRequest;
import com.zeta.rider_service.enums.PaymentMethod;
import com.zeta.rider_service.enums.PaymentStatus;
import com.zeta.rider_service.exception.GlobalExceptionHandler;
import com.zeta.rider_service.exception.PaymentNotFoundException;
import com.zeta.rider_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testProcessPayment_Success_ShouldReturnCreated() throws Exception {
        // Arrange
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId(100L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        PaymentDTO paymentDTO = PaymentDTO.builder()
                .paymentId(1L)
                .rideId(100L)
                .riderId(1L)
                .amount(BigDecimal.valueOf(60.0))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN123456")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class))).thenReturn(paymentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1L))
                .andExpect(jsonPath("$.rideId").value(100L))
                .andExpect(jsonPath("$.riderId").value(1L))
                .andExpect(jsonPath("$.amount").value(60.0))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionId").value("TXN123456"));

        verify(paymentService).processPayment(any(ProcessPaymentRequest.class));
    }

    @Test
    void testProcessPayment_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId(null) // Invalid: null rideId
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).processPayment(any());
    }

    @Test
    void testGetPaymentByRide_Success_ShouldReturnPayment() throws Exception {
        // Arrange
        Long rideId = 100L;
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .paymentId(1L)
                .rideId(rideId)
                .riderId(1L)
                .amount(BigDecimal.valueOf(60.0))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN123456")
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.getPaymentByRide(rideId)).thenReturn(paymentDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/ride/{rideId}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1L))
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.amount").value(60.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(paymentService).getPaymentByRide(rideId);
    }

    @Test
    void testGetPaymentByRide_NotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long rideId = 999L;
        when(paymentService.getPaymentByRide(rideId))
                .thenThrow(new PaymentNotFoundException("Payment not found for ride: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/ride/{rideId}", rideId))
                .andExpect(status().isNotFound());

        verify(paymentService).getPaymentByRide(rideId);
    }

    @Test
    void testGetPaymentsByRider_Success_ShouldReturnPaymentList() throws Exception {
        // Arrange
        Long riderId = 1L;
        PaymentDTO payment1 = PaymentDTO.builder()
                .paymentId(1L)
                .rideId(100L)
                .riderId(riderId)
                .amount(BigDecimal.valueOf(60.0))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN123456")
                .paymentDate(LocalDateTime.now())
                .build();

        PaymentDTO payment2 = PaymentDTO.builder()
                .paymentId(2L)
                .rideId(101L)
                .riderId(riderId)
                .amount(BigDecimal.valueOf(75.0))
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.COMPLETED)
                .transactionId("TXN789012")
                .paymentDate(LocalDateTime.now())
                .build();

        List<PaymentDTO> payments = Arrays.asList(payment1, payment2);
        when(paymentService.getPaymentsByRider(riderId)).thenReturn(payments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/rider/{riderId}", riderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].paymentId").value(1L))
                .andExpect(jsonPath("$[0].rideId").value(100L))
                .andExpect(jsonPath("$[1].paymentId").value(2L))
                .andExpect(jsonPath("$[1].rideId").value(101L));

        verify(paymentService).getPaymentsByRider(riderId);
    }

    @Test
    void testGetPaymentsByRider_EmptyList_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        Long riderId = 1L;
        when(paymentService.getPaymentsByRider(riderId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/rider/{riderId}", riderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(paymentService).getPaymentsByRider(riderId);
    }

    @Test
    void testProcessPayment_WithCash_ShouldReturnCreated() throws Exception {
        // Arrange
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .rideId(100L)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        PaymentDTO paymentDTO = PaymentDTO.builder()
                .paymentId(1L)
                .rideId(100L)
                .riderId(1L)
                .amount(BigDecimal.valueOf(60.0))
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class))).thenReturn(paymentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentService).processPayment(any(ProcessPaymentRequest.class));
    }
}
