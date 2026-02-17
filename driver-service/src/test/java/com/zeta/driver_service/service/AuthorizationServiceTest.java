package com.zeta.driver_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentUserId_WhenAuthenticated_ReturnsUserId() {
        // Arrange
        Long expectedUserId = 123L;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(expectedUserId.toString());

        // Act
        Long result = authorizationService.getCurrentUserId();

        // Assert
        assertEquals(expectedUserId, result);
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication).getName();
    }

    @Test
    void testGetCurrentUserId_WhenNotAuthenticated_ThrowsAccessDeniedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> authorizationService.getCurrentUserId());
        verify(securityContext).getAuthentication();
    }

    @Test
    void testGetCurrentUserId_WhenAuthenticationNotAuthenticated_ThrowsAccessDeniedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> authorizationService.getCurrentUserId());
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
    }

    @Test
    void testVerifyDriverOwnership_WhenAdmin_DoesNotThrowException() {
        // Arrange
        Long driverId = 456L;
        Long currentUserId = 123L;
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(currentUserId.toString());
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act & Assert
        assertDoesNotThrow(() -> authorizationService.verifyDriverOwnership(driverId));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication).getName();
        verify(authentication).getAuthorities();
    }

    @Test
    void testVerifyDriverOwnership_WhenSameDriver_DoesNotThrowException() {
        // Arrange
        Long driverId = 123L;
        Long currentUserId = 123L;
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(currentUserId.toString());
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act & Assert
        assertDoesNotThrow(() -> authorizationService.verifyDriverOwnership(driverId));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication).getName();
        verify(authentication).getAuthorities();
    }

    @Test
    void testVerifyDriverOwnership_WhenDifferentDriver_ThrowsAccessDeniedException() {
        // Arrange
        Long driverId = 456L;
        Long currentUserId = 123L;
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(currentUserId.toString());
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> authorizationService.verifyDriverOwnership(driverId));
        assertEquals("You can only access your own data", exception.getMessage());
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication).getName();
        verify(authentication).getAuthorities();
    }

    @Test
    void testVerifyDriverOwnership_WhenNotAuthenticated_ThrowsAccessDeniedException() {
        // Arrange
        Long driverId = 123L;
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> authorizationService.verifyDriverOwnership(driverId));
        verify(securityContext).getAuthentication();
    }

    @Test
    void testIsDriver_WhenHasDriverRole_ReturnsTrue() {
        // Arrange
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act
        boolean result = authorizationService.isDriver();

        // Assert
        assertTrue(result);
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();
    }

    @Test
    void testIsDriver_WhenDoesNotHaveDriverRole_ReturnsFalse() {
        // Arrange
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act
        boolean result = authorizationService.isDriver();

        // Assert
        assertFalse(result);
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();
    }

    @Test
    void testIsDriver_WhenNoAuthentication_ReturnsFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = authorizationService.isDriver();

        // Assert
        assertFalse(result);
        verify(securityContext).getAuthentication();
        verify(authentication, never()).getAuthorities();
    }

    @Test
    void testIsDriver_WhenMultipleAuthorities_ReturnsTrueIfDriverRolePresent() {
        // Arrange
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_DRIVER"));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act
        boolean result = authorizationService.isDriver();

        // Assert
        assertTrue(result);
        verify(securityContext).getAuthentication();
        verify(authentication).getAuthorities();
    }
}
