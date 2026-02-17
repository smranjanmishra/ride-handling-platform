package com.zeta.driver_service.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    @Test
    void testHandleValidationExceptions_ReturnsBadRequest() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError1 = new FieldError("createDriverRequest", "email", "Email is required");
        FieldError fieldError2 = new FieldError("createDriverRequest", "password", "Password is required");
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        errors.add(fieldError1);
        errors.add(fieldError2);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

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
        assertTrue(response.getBody().getMessage().contains("email"));
        assertTrue(response.getBody().getMessage().contains("password"));
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleDriverNotFoundException_ReturnsNotFound() {
        // Arrange
        String errorMessage = "Driver not found: 1";
        DriverNotFoundException ex = new DriverNotFoundException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleDriverNotFoundException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Driver Not Found", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleBusinessLogicException_DuplicateEmailException_ReturnsBadRequest() {
        // Arrange
        String errorMessage = "Email already registered";
        DuplicateEmailException ex = new DuplicateEmailException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Business Logic Error", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleBusinessLogicException_DriverNotAvailableException_ReturnsBadRequest() {
        // Arrange
        String errorMessage = "Driver is not available";
        DriverNotAvailableException ex = new DriverNotAvailableException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleBusinessLogicException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Business Logic Error", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleAccessDeniedException_ReturnsForbidden() {
        // Arrange
        String errorMessage = "You can only access your own data";
        AccessDeniedException ex = new AccessDeniedException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleAccessDeniedException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
        assertEquals("Access Denied", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleOptimisticLockException_OptimisticLockException_ReturnsConflict() {
        // Arrange
        OptimisticLockException ex = new OptimisticLockException();

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleOptimisticLockException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertEquals("The resource was modified by another request. Please retry.", 
                response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleOptimisticLockException_OptimisticLockingFailureException_ReturnsConflict() {
        // Arrange
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Concurrent modification");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleOptimisticLockException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Concurrent Modification", response.getBody().getError());
        assertEquals("The resource was modified by another request. Please retry.", 
                response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleInvalidCredentials_ReturnsUnauthorized() {
        // Arrange
        String errorMessage = "Invalid email or password";
        InvalidCredentialsException ex = new InvalidCredentialsException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleInvalidCredentials(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleGlobalException_ReturnsInternalServerError() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("uri=/api/v1/test", response.getBody().getPath());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleGlobalException_NullPointerException_ReturnsInternalServerError() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null pointer");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGlobalException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        verify(webRequest).getDescription(false);
    }

    @Test
    void testHandleValidationExceptions_EmptyErrors_ReturnsBadRequest() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

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
        verify(webRequest).getDescription(false);
    }

    @Test
    void testErrorResponse_AllFieldsSet() {
        // Arrange
        String errorMessage = "Test error";
        DriverNotFoundException ex = new DriverNotFoundException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleDriverNotFoundException(ex, webRequest);

        // Assert
        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
    }

    @Test
    void testHandleValidationExceptions_MultipleFieldErrors_AllIncluded() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        
        FieldError fieldError1 = new FieldError("request", "field1", "Field1 error");
        FieldError fieldError2 = new FieldError("request", "field2", "Field2 error");
        FieldError fieldError3 = new FieldError("request", "field3", "Field3 error");
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        errors.add(fieldError1);
        errors.add(fieldError2);
        errors.add(fieldError3);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleValidationExceptions(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String message = response.getBody().getMessage();
        assertTrue(message.contains("field1"));
        assertTrue(message.contains("field2"));
        assertTrue(message.contains("field3"));
        verify(webRequest).getDescription(false);
    }
}
