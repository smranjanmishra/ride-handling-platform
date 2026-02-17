package com.zeta.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.notification_service.dto.NotificationRequest;
import com.zeta.notification_service.dto.NotificationResponse;
import com.zeta.notification_service.dto.RideAcceptedNotificationRequest;
import com.zeta.notification_service.enums.NotificationType;
import com.zeta.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
    }

    // ========== Ride Accepted Notification Tests ==========

    @Test
    void testSendRideAcceptedNotification_Success() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName("John Doe");

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_ACCEPTED)
                .message("Your ride #1 has been accepted by driver John Doe")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideAcceptedNotification(1L, "John Doe")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.rideId").value(1L))
                .andExpect(jsonPath("$.type").value("RIDE_ACCEPTED"))
                .andExpect(jsonPath("$.message").value("Your ride #1 has been accepted by driver John Doe"))
                .andExpect(jsonPath("$.sentAt").exists());

        verify(notificationService, times(1)).sendRideAcceptedNotification(1L, "John Doe");
    }

    @Test
    void testSendRideAcceptedNotification_WithNullRideId() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(null);
        request.setDriverName("John Doe");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendRideAcceptedNotification(any(), any());
    }

    @Test
    void testSendRideAcceptedNotification_WithNullDriverName() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName(null);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendRideAcceptedNotification(any(), any());
    }

    @Test
    void testSendRideAcceptedNotification_WithEmptyDriverName() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName("");

        // Act & Assert
        // Empty string might pass validation depending on @NotNull vs @NotBlank
        // If it passes, service will be called
        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_ACCEPTED)
                .message("Your ride #1 has been accepted by driver ")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideAcceptedNotification(1L, "")).thenReturn(response);

        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService, times(1)).sendRideAcceptedNotification(1L, "");
    }

    @Test
    void testSendRideAcceptedNotification_WithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendRideAcceptedNotification(any(), any());
    }

    @Test
    void testSendRideAcceptedNotification_WithMissingContentType() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName("John Doe");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        verify(notificationService, never()).sendRideAcceptedNotification(any(), any());
    }

    @Test
    void testSendRideAcceptedNotification_WhenServiceThrowsException() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName("John Doe");

        when(notificationService.sendRideAcceptedNotification(1L, "John Doe"))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(notificationService, times(1)).sendRideAcceptedNotification(1L, "John Doe");
    }

    // ========== Ride Completed Notification Tests ==========

    @Test
    void testSendRideCompletedNotification_Success() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        NotificationResponse response = NotificationResponse.builder()
                .id(2L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Your ride #1 has been completed. Thank you for riding with us!")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.rideId").value(1L))
                .andExpect(jsonPath("$.type").value("RIDE_COMPLETED"))
                .andExpect(jsonPath("$.message").value("Your ride #1 has been completed. Thank you for riding with us!"))
                .andExpect(jsonPath("$.sentAt").exists());

        verify(notificationService, times(1)).sendRideCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendRideCompletedNotification_WithNullUserId() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(null);
        request.setRideId(1L);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendRideCompletedNotification(any());
    }

    @Test
    void testSendRideCompletedNotification_WithNullRideId() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(null);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendRideCompletedNotification(any());
    }

    @Test
    void testSendRideCompletedNotification_WithAllFields() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(200L);
        request.setRideId(5L);
        request.setDriverName("Jane Driver");
        request.setRiderName("John Rider");
        request.setAmount(150.75);

        NotificationResponse response = NotificationResponse.builder()
                .id(3L)
                .userId(200L)
                .rideId(5L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Your ride #5 has been completed. Thank you for riding with us!")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value(5L))
                .andExpect(jsonPath("$.userId").value(200L));

        verify(notificationService, times(1)).sendRideCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendRideCompletedNotification_WhenServiceThrowsException() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        when(notificationService.sendRideCompletedNotification(any(NotificationRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(notificationService, times(1)).sendRideCompletedNotification(any(NotificationRequest.class));
    }

    // ========== Payment Completed Notification Tests ==========

    @Test
    void testSendPaymentCompletedNotification_Success() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(250.50);

        NotificationResponse response = NotificationResponse.builder()
                .id(4L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹250.50 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(4L))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.rideId").value(1L))
                .andExpect(jsonPath("$.type").value("PAYMENT_COMPLETED"))
                .andExpect(jsonPath("$.message").value("Payment of ₹250.50 for ride #1 has been completed successfully"))
                .andExpect(jsonPath("$.sentAt").exists());

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithNullUserId() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(null);
        request.setRideId(1L);
        request.setAmount(250.50);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendPaymentCompletedNotification(any());
    }

    @Test
    void testSendPaymentCompletedNotification_WithNullRideId() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(null);
        request.setAmount(250.50);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendPaymentCompletedNotification(any());
    }

    @Test
    void testSendPaymentCompletedNotification_WithNullAmount() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(null);

        // Act & Assert
        // Amount is not @NotNull in NotificationRequest, so validation will pass
        // But service will throw NPE when String.format tries to unbox null Double
        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class)))
                .thenThrow(new NullPointerException("Cannot unbox null Double"));

        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithZeroAmount() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(0.0);

        NotificationResponse response = NotificationResponse.builder()
                .id(6L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹0.00 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Payment of ₹0.00 for ride #1 has been completed successfully"));

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithLargeAmount() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(999999.99);

        NotificationResponse response = NotificationResponse.builder()
                .id(7L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹999999.99 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Payment of ₹999999.99 for ride #1 has been completed successfully"));

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WhenServiceThrowsException() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(250.50);

        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class)))
                .thenThrow(new RuntimeException("Payment processing error"));

        // Act & Assert
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }

    // ========== General Controller Tests ==========

    @Test
    void testSendRideAcceptedNotification_VerifyRequestMapping() throws Exception {
        // Arrange
        RideAcceptedNotificationRequest request = new RideAcceptedNotificationRequest();
        request.setRideId(1L);
        request.setDriverName("Test Driver");

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_ACCEPTED)
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideAcceptedNotification(1L, "Test Driver")).thenReturn(response);

        // Act & Assert - Verify the endpoint path
        mockMvc.perform(post("/api/notifications/ride-accepted")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService, times(1)).sendRideAcceptedNotification(eq(1L), eq("Test Driver"));
    }

    @Test
    void testSendRideCompletedNotification_VerifyRequestMapping() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendRideCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert - Verify the endpoint path
        mockMvc.perform(post("/api/notifications/ride-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService, times(1)).sendRideCompletedNotification(any(NotificationRequest.class));
    }

    @Test
    void testSendPaymentCompletedNotification_VerifyRequestMapping() throws Exception {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(100.0);

        NotificationResponse response = NotificationResponse.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Test message")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationService.sendPaymentCompletedNotification(any(NotificationRequest.class))).thenReturn(response);

        // Act & Assert - Verify the endpoint path
        mockMvc.perform(post("/api/notifications/payment-completed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService, times(1)).sendPaymentCompletedNotification(any(NotificationRequest.class));
    }
}
