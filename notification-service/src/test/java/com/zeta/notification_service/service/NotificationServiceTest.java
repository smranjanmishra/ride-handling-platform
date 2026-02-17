package com.zeta.notification_service.service;

import com.zeta.notification_service.client.RiderClient;
import com.zeta.notification_service.dto.NotificationRequest;
import com.zeta.notification_service.dto.NotificationResponse;
import com.zeta.notification_service.entity.Notification;
import com.zeta.notification_service.entity.RideDetails;
import com.zeta.notification_service.enums.NotificationType;
import com.zeta.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RiderClient riderClient;

    @InjectMocks
    private NotificationService notificationService;

    private RideDetails rideDetails;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        rideDetails = new RideDetails();
        rideDetails.setRideId(1L);
        rideDetails.setRiderId(100L);

        savedNotification = Notification.builder()
                .id(1L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_ACCEPTED)
                .message("Your ride #1 has been accepted by driver John Doe")
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testSendRideAcceptedNotification_Success() {
        // Arrange
        Long rideId = 1L;
        String driverName = "John Doe";
        Long userId = 100L;

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertEquals(savedNotification.getId(), response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(rideId, response.getRideId());
        assertEquals(NotificationType.RIDE_ACCEPTED, response.getType());
        assertEquals("Your ride #1 has been accepted by driver John Doe", response.getMessage());
        assertNotNull(response.getSentAt());

        // Verify interactions
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WithDifferentRideId() {
        // Arrange
        Long rideId = 2L;
        String driverName = "Jane Smith";
        Long userId = 200L;

        RideDetails differentRide = new RideDetails();
        differentRide.setRideId(2L);
        differentRide.setRiderId(200L);

        Notification differentNotification = Notification.builder()
                .id(2L)
                .userId(200L)
                .rideId(2L)
                .type(NotificationType.RIDE_ACCEPTED)
                .message("Your ride #2 has been accepted by driver Jane Smith")
                .sentAt(LocalDateTime.now())
                .build();

        when(riderClient.getRide(rideId)).thenReturn(differentRide);
        when(notificationRepository.save(any(Notification.class))).thenReturn(differentNotification);

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(userId, response.getUserId());
        assertEquals(rideId, response.getRideId());
        assertEquals(NotificationType.RIDE_ACCEPTED, response.getType());
        assertEquals("Your ride #2 has been accepted by driver Jane Smith", response.getMessage());

        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideCompletedNotification_Success() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        Notification completedNotification = Notification.builder()
                .id(2L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Your ride #1 has been completed. Thank you for riding with us!")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(completedNotification);

        // Act
        NotificationResponse response = notificationService.sendRideCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(completedNotification.getId(), response.getId());
        assertEquals(100L, response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.RIDE_COMPLETED, response.getType());
        assertEquals("Your ride #1 has been completed. Thank you for riding with us!", response.getMessage());
        assertNotNull(response.getSentAt());

        // Verify interactions
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(riderClient, never()).getRide(anyLong());
    }

    @Test
    void testSendRideCompletedNotification_WithDifferentRideId() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(200L);
        request.setRideId(5L);

        Notification completedNotification = Notification.builder()
                .id(3L)
                .userId(200L)
                .rideId(5L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Your ride #5 has been completed. Thank you for riding with us!")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(completedNotification);

        // Act
        NotificationResponse response = notificationService.sendRideCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(5L, response.getRideId());
        assertEquals(200L, response.getUserId());
        assertEquals("Your ride #5 has been completed. Thank you for riding with us!", response.getMessage());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_Success() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(250.50);

        Notification paymentNotification = Notification.builder()
                .id(3L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹250.50 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(paymentNotification.getId(), response.getId());
        assertEquals(100L, response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.PAYMENT_COMPLETED, response.getType());
        assertEquals("Payment of ₹250.50 for ride #1 has been completed successfully", response.getMessage());
        assertNotNull(response.getSentAt());

        // Verify interactions
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(riderClient, never()).getRide(anyLong());
    }

    @Test
    void testSendPaymentCompletedNotification_WithDifferentAmount() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(300L);
        request.setRideId(10L);
        request.setAmount(500.75);

        Notification paymentNotification = Notification.builder()
                .id(4L)
                .userId(300L)
                .rideId(10L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹500.75 for ride #10 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getRideId());
        assertEquals(300L, response.getUserId());
        assertEquals("Payment of ₹500.75 for ride #10 has been completed successfully", response.getMessage());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithZeroAmount() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(0.00);

        Notification paymentNotification = Notification.builder()
                .id(5L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹0.00 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals("Payment of ₹0.00 for ride #1 has been completed successfully", response.getMessage());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_VerifyNotificationFields() {
        // Arrange
        Long rideId = 1L;
        String driverName = "Test Driver";
        Long userId = 100L;

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(rideId, response.getRideId());
        assertEquals(NotificationType.RIDE_ACCEPTED, response.getType());
        assertTrue(response.getMessage().contains("Your ride #1 has been accepted by driver Test Driver"));

        // Verify the notification was saved with correct fields
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId().equals(userId) &&
                notification.getRideId().equals(rideId) &&
                notification.getType().equals(NotificationType.RIDE_ACCEPTED) &&
                notification.getMessage().contains("Test Driver")
        ));
    }

    @Test
    void testSendRideCompletedNotification_VerifyNotificationFields() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.RIDE_COMPLETED, response.getType());

        // Verify the notification was saved with correct fields
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId().equals(100L) &&
                notification.getRideId().equals(1L) &&
                notification.getType().equals(NotificationType.RIDE_COMPLETED) &&
                notification.getMessage().contains("has been completed")
        ));
    }

    @Test
    void testSendPaymentCompletedNotification_VerifyNotificationFields() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(100.25);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.PAYMENT_COMPLETED, response.getType());
        assertTrue(response.getMessage().contains("₹100.25"));

        // Verify the notification was saved with correct fields
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId().equals(100L) &&
                notification.getRideId().equals(1L) &&
                notification.getType().equals(NotificationType.PAYMENT_COMPLETED) &&
                notification.getMessage().contains("100.25")
        ));
    }

    // ========== Exception Handling Test Cases ==========

    @Test
    void testSendRideAcceptedNotification_WhenRiderClientThrowsException() {
        // Arrange
        Long rideId = 1L;
        String driverName = "John Doe";

        when(riderClient.getRide(rideId)).thenThrow(new RuntimeException("Ride service unavailable"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendRideAcceptedNotification(rideId, driverName);
        });

        // Verify repository was never called
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(riderClient, times(1)).getRide(rideId);
    }

    @Test
    void testSendRideAcceptedNotification_WhenRideDetailsIsNull() {
        // Arrange
        Long rideId = 1L;
        String driverName = "John Doe";

        when(riderClient.getRide(rideId)).thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendRideAcceptedNotification(rideId, driverName);
        });

        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WhenRiderIdIsNull() {
        // Arrange
        Long rideId = 1L;
        String driverName = "John Doe";

        RideDetails rideWithNullRiderId = new RideDetails();
        rideWithNullRiderId.setRideId(rideId);
        rideWithNullRiderId.setRiderId(null);

        when(riderClient.getRide(rideId)).thenReturn(rideWithNullRiderId);
        // The service doesn't throw exception when riderId is null, it just creates notification with null userId
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert - Verify that notification was created with null userId (service doesn't validate)
        assertNotNull(response);
        assertNull(response.getUserId());
        assertEquals(rideId, response.getRideId());
        assertEquals(NotificationType.RIDE_ACCEPTED, response.getType());
        assertTrue(response.getMessage().contains("Your ride #1 has been accepted by driver John Doe"));

        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId() == null &&
                notification.getRideId().equals(rideId)
        ));
    }

    @Test
    void testSendRideCompletedNotification_WhenRepositoryThrowsException() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);

        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendRideCompletedNotification(request);
        });

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WhenRepositoryThrowsException() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(250.50);

        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendPaymentCompletedNotification(request);
        });

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ========== Null/Invalid Input Test Cases ==========

    @Test
    void testSendRideAcceptedNotification_WithNullRideId() {
        // Arrange
        Long rideId = null;
        String driverName = "John Doe";

        // Mock riderClient to return null when called with null rideId
        when(riderClient.getRide(any())).thenReturn(null);

        // Act & Assert
        // NullPointerException will be thrown when trying to call getRiderId() on null ride
        // (happens before String.format)
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendRideAcceptedNotification(rideId, driverName);
        });

        verify(riderClient, times(1)).getRide(any());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WithNullDriverName() {
        // Arrange
        Long rideId = 1L;
        String driverName = null;

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        // Use thenAnswer to return the actual notification that was passed in, preserving the generated message
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert - Should still work but message will contain "null"
        assertNotNull(response);
        assertTrue(response.getMessage().contains("null"));
        assertEquals("Your ride #1 has been accepted by driver null", response.getMessage());
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WithEmptyDriverName() {
        // Arrange
        Long rideId = 1L;
        String driverName = "";

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("Your ride #1 has been accepted by driver "));
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideCompletedNotification_WithNullRequest() {
        // Arrange
        NotificationRequest request = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendRideCompletedNotification(request);
        });

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testSendRideCompletedNotification_WithNullUserId() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(null);
        request.setRideId(1L);

        Notification notificationWithNullUserId = Notification.builder()
                .id(1L)
                .userId(null)
                .rideId(1L)
                .type(NotificationType.RIDE_COMPLETED)
                .message("Your ride #1 has been completed. Thank you for riding with us!")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationWithNullUserId);

        // Act
        // The service doesn't validate null userId, so it will proceed and create notification with null userId
        NotificationResponse response = notificationService.sendRideCompletedNotification(request);

        // Assert - Verify that notification was created with null userId
        assertNotNull(response);
        assertNull(response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.RIDE_COMPLETED, response.getType());
        
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId() == null &&
                notification.getRideId().equals(1L)
        ));
    }

    @Test
    void testSendPaymentCompletedNotification_WithNullRequest() {
        // Arrange
        NotificationRequest request = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            notificationService.sendPaymentCompletedNotification(request);
        });

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithNullUserId() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(null);
        request.setRideId(1L);
        request.setAmount(250.50);

        Notification notificationWithNullUserId = Notification.builder()
                .id(1L)
                .userId(null)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹250.50 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationWithNullUserId);

        // Act
        // The service doesn't validate null userId, so it will proceed and create notification with null userId
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert - Verify that notification was created with null userId
        assertNotNull(response);
        assertNull(response.getUserId());
        assertEquals(1L, response.getRideId());
        assertEquals(NotificationType.PAYMENT_COMPLETED, response.getType());
        assertTrue(response.getMessage().contains("250.50"));
        
        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getUserId() == null &&
                notification.getRideId().equals(1L)
        ));
    }

    // ========== Edge Cases Test Cases ==========

    @Test
    void testSendRideAcceptedNotification_WithSpecialCharactersInDriverName() {
        // Arrange
        Long rideId = 1L;
        String driverName = "O'Brien & O'Connell";

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("O'Brien & O'Connell"));
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WithVeryLongDriverName() {
        // Arrange
        Long rideId = 1L;
        String driverName = "A".repeat(500); // Very long name

        when(riderClient.getRide(rideId)).thenReturn(rideDetails);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains(driverName));
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithNegativeAmount() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(-100.50);

        Notification paymentNotification = Notification.builder()
                .id(6L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹-100.50 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("-100.50"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithVeryLargeAmount() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(999999999.99);

        Notification paymentNotification = Notification.builder()
                .id(7L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹999999999.99 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("999999999.99"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_WithDecimalPrecision() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(1L);
        request.setAmount(123.456789);

        Notification paymentNotification = Notification.builder()
                .id(8L)
                .userId(100L)
                .rideId(1L)
                .type(NotificationType.PAYMENT_COMPLETED)
                .message("Payment of ₹123.46 for ride #1 has been completed successfully")
                .sentAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(paymentNotification);

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert - Should format to 2 decimal places
        assertNotNull(response);
        assertTrue(response.getMessage().contains("123.46") || response.getMessage().contains("123.45"));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideAcceptedNotification_WithVeryLargeRideId() {
        // Arrange
        Long rideId = Long.MAX_VALUE;
        String driverName = "John Doe";

        RideDetails largeRide = new RideDetails();
        largeRide.setRideId(rideId);
        largeRide.setRiderId(100L);

        Notification largeNotification = Notification.builder()
                .id(1L)
                .userId(100L)
                .rideId(rideId)
                .type(NotificationType.RIDE_ACCEPTED)
                .message(String.format("Your ride #%d has been accepted by driver %s", rideId, driverName))
                .sentAt(LocalDateTime.now())
                .build();

        when(riderClient.getRide(rideId)).thenReturn(largeRide);
        when(notificationRepository.save(any(Notification.class))).thenReturn(largeNotification);

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert
        assertNotNull(response);
        assertEquals(rideId, response.getRideId());
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ========== Message Formatting Test Cases ==========

    @Test
    void testSendRideAcceptedNotification_MessageFormatting() {
        // Arrange
        Long rideId = 12345L;
        String driverName = "Test Driver";

        RideDetails testRide = new RideDetails();
        testRide.setRideId(rideId);
        testRide.setRiderId(100L);

        when(riderClient.getRide(rideId)).thenReturn(testRide);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideAcceptedNotification(rideId, driverName);

        // Assert - Verify exact message format
        String expectedMessage = String.format("Your ride #%d has been accepted by driver %s", rideId, driverName);
        assertEquals(expectedMessage, response.getMessage());
        verify(riderClient, times(1)).getRide(rideId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendRideCompletedNotification_MessageFormatting() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(999L);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendRideCompletedNotification(request);

        // Assert - Verify exact message format
        String expectedMessage = "Your ride #999 has been completed. Thank you for riding with us!";
        assertEquals(expectedMessage, response.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSendPaymentCompletedNotification_MessageFormatting() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(100L);
        request.setRideId(42L);
        request.setAmount(99.99);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(1L);
            notification.setSentAt(LocalDateTime.now());
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendPaymentCompletedNotification(request);

        // Assert - Verify exact message format with currency symbol and 2 decimal places
        String expectedMessage = "Payment of ₹99.99 for ride #42 has been completed successfully";
        assertEquals(expectedMessage, response.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
