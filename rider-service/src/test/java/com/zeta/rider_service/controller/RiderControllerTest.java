package com.zeta.rider_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.rider_service.dto.CreateRiderRequest;
import com.zeta.rider_service.dto.RiderDTO;
import com.zeta.rider_service.enums.RiderStatus;
import com.zeta.rider_service.exception.DuplicateEmailException;
import com.zeta.rider_service.exception.DuplicatePhoneException;
import com.zeta.rider_service.exception.GlobalExceptionHandler;
import com.zeta.rider_service.exception.RiderNotFoundException;
import com.zeta.rider_service.service.RiderService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RiderControllerTest {

    @Mock
    private RiderService riderService;

    @InjectMocks
    private RiderController riderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(riderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterRider_Success_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        RiderDTO riderDTO = RiderDTO.builder()
                .riderId(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .totalRides(0)
                .status(RiderStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(riderService.createRider(any(CreateRiderRequest.class))).thenReturn(riderDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riderId").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.totalRides").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(riderService).createRider(any(CreateRiderRequest.class));
    }

    @Test
    void testRegisterRider_DuplicateEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("John Doe")
                .email("existing@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        when(riderService.createRider(any(CreateRiderRequest.class)))
                .thenThrow(new DuplicateEmailException("Email already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(riderService).createRider(any(CreateRiderRequest.class));
    }

    @Test
    void testRegisterRider_DuplicatePhone_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("9999999999")
                .password("password123")
                .build();

        when(riderService.createRider(any(CreateRiderRequest.class)))
                .thenThrow(new DuplicatePhoneException("Phone already registered"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(riderService).createRider(any(CreateRiderRequest.class));
    }

    @Test
    void testRegisterRider_MissingName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("") // Invalid: empty name
                .email("john@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(riderService, never()).createRider(any());
    }

    @Test
    void testRegisterRider_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("John Doe")
                .email("invalid-email") // Invalid: not a valid email format
                .phone("1234567890")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(riderService, never()).createRider(any());
    }

    @Test
    void testRegisterRider_MissingPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRiderRequest request = CreateRiderRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .password("") // Invalid: empty password
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/riders/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(riderService, never()).createRider(any());
    }

    @Test
    void testGetRider_Success_ShouldReturnRider() throws Exception {
        // Arrange
        Long riderId = 1L;
        RiderDTO riderDTO = RiderDTO.builder()
                .riderId(riderId)
                .name("John Doe")
                .email("john@example.com")
                .phone("1234567890")
                .totalRides(5)
                .status(RiderStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(riderService.getRider(riderId)).thenReturn(riderDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/riders/{riderId}", riderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riderId").value(riderId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.totalRides").value(5));

        verify(riderService).getRider(riderId);
    }

    @Test
    void testGetRider_NotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long riderId = 999L;
        when(riderService.getRider(riderId))
                .thenThrow(new RiderNotFoundException("Rider not found: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/riders/{riderId}", riderId))
                .andExpect(status().isNotFound());

        verify(riderService).getRider(riderId);
    }
}
