package com.zeta.rider_service.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    @Test
    void testHandleValidationExceptions_ShouldReturnBadRequest() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        
        FieldError fieldError1 = new FieldError("createRideRequest", "pickupLatitude", "Pickup latitude is required");
        FieldError fieldError2 = new FieldError("createRideRequest", "riderId", "Rider ID is required");
        fieldErrors.add(fieldError1);
        fieldErrors.add(fieldError2);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationExceptions(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("pickupLatitude"));
        assertTrue(response.getBody().getMessage().contains("riderId"));
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
    }

    @Test
    void testHandleNotFoundException_RiderNotFound() {
        // Arrange
        RiderNotFoundException ex = new RiderNotFoundException("Rider not found: 999");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertEquals("Rider not found: 999", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleNotFoundException_RideNotFound() {
        // Arrange
        RideNotFoundException ex = new RideNotFoundException("Ride not found: 100");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Ride not found: 100", response.getBody().getMessage());
    }

    @Test
    void testHandleNotFoundException_PaymentNotFound() {
        // Arrange
        PaymentNotFoundException ex = new PaymentNotFoundException("Payment not found for ride: 100");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Payment not found for ride: 100", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessLogicException_OngoingRideExists() {
        // Arrange
        OngoingRideExistsException ex = new OngoingRideExistsException("You already have an ongoing ride");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Business Logic Error", response.getBody().getError());
        assertEquals("You already have an ongoing ride", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessLogicException_InvalidRideRequest() {
        // Arrange
        InvalidRideRequestException ex = new InvalidRideRequestException("Pickup and drop locations cannot be the same");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Pickup and drop locations cannot be the same", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessLogicException_InvalidStateTransition() {
        // Arrange
        InvalidStateTransitionException ex = new InvalidStateTransitionException("Invalid state transition from REQUESTED to COMPLETED");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Invalid state transition"));
    }

    @Test
    void testHandleBusinessLogicException_DuplicateEmail() {
        // Arrange
        DuplicateEmailException ex = new DuplicateEmailException("Email already registered");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already registered", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessLogicException_DuplicatePhone() {
        // Arrange
        DuplicatePhoneException ex = new DuplicatePhoneException("Phone already registered");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Phone already registered", response.getBody().getMessage());
    }

    @Test
    void testHandleBusinessLogicException_InvalidRideState() {
        // Arrange
        InvalidRideStateException ex = new InvalidRideStateException("Cannot cancel a completed ride");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cannot cancel a completed ride", response.getBody().getMessage());
    }

    @Test
    void testHandlePaymentException_ShouldReturnInternalServerError() {
        // Arrange
        PaymentProcessingException ex = new PaymentProcessingException("Payment failed: Database error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handlePaymentException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Payment Processing Failed", response.getBody().getError());
        assertEquals("Payment failed: Database error", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleAccessDeniedException_ShouldReturnForbidden() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("You can only access your own rides");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals("You can only access your own rides", response.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleOptimisticLockException_ShouldReturnConflict() {
        // Arrange
        OptimisticLockException ex = new OptimisticLockException();

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleOptimisticLockException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertEquals("The resource was modified by another request. Please retry.", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleOptimisticLockException_OptimisticLockingFailureException() {
        // Arrange
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Optimistic locking failure", null);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleOptimisticLockException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertEquals("The resource was modified by another request. Please retry.", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalStateException_ShouldReturnBadRequest() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("No drivers available nearby");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleIllegalStateException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid State", response.getBody().getError());
        assertEquals("No drivers available nearby", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
    }

    @Test
    void testHandleGlobalException_ShouldReturnInternalServerError() {
        // Arrange
        RuntimeException ex = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
    }

    @Test
    void testErrorResponse_ShouldHaveAllFields() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        Integer status = 404;
        String error = "Not Found";
        String message = "Resource not found";
        String path = "/api/v1/rides/100";

        // Act
        GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(timestamp, status, error, message, path);

        // Assert
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    @Test
    void testHandleValidationExceptions_WithEmptyErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationExceptions(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().getMessage());
    }

    @Test
    void testHandleGlobalException_WithNullPointerException() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null pointer");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void testAllExceptionHandlers_SetTimestamp() {
        // Arrange
        RiderNotFoundException ex = new RiderNotFoundException("Test");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(response.getBody().getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testAllExceptionHandlers_SetPath() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/riders/1");
        RiderNotFoundException ex = new RiderNotFoundException("Test");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleNotFoundException(ex, webRequest);

        // Assert
        assertEquals("uri=/api/v1/riders/1", response.getBody().getPath());
    }
}
