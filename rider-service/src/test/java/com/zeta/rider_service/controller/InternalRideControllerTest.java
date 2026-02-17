package com.zeta.rider_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.rider_service.dto.RideDTO;
import com.zeta.rider_service.dto.RideInternalDTO;
import com.zeta.rider_service.dto.UpdateRideStatusRequest;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.GlobalExceptionHandler;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.service.RideService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InternalRideControllerTest {

    @Mock
    private RideService rideService;

    private InternalRideController internalRideController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(rideService);
        internalRideController = new InternalRideController(rideService);
        mockMvc = MockMvcBuilders.standaloneSetup(internalRideController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testUpdateRideStatus_Success_ShouldReturnOk() throws Exception {
        // Arrange
        Long rideId = 100L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(RideStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();

        RideDTO rideDTO = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.STARTED)
                .startedAt(LocalDateTime.now())
                .build();

        when(rideService.updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class))).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.driverId").value(2L));

        verify(rideService, times(1)).updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class));
    }

    @Test
    void testUpdateRideStatus_ToCompleted_ShouldReturnOk() throws Exception {
        // Arrange
        Long rideId = 100L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(RideStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();

        RideDTO rideDTO = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .fareAmount(BigDecimal.valueOf(60.0))
                .build();

        when(rideService.updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class))).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fareAmount").value(60.0));

        verify(rideService, times(1)).updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class));
    }

    @Test
    void testUpdateRideStatus_ToAssigned_ShouldReturnOk() throws Exception {
        // Arrange
        Long rideId = 100L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(RideStatus.DRIVER_ASSIGNED)
                .driverId(2L)
                .assignedAt(LocalDateTime.now())
                .build();

        RideDTO rideDTO = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.DRIVER_ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        when(rideService.updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class))).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("DRIVER_ASSIGNED"))
                .andExpect(jsonPath("$.driverId").value(2L));

        verify(rideService, times(1)).updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class));
    }

    @Test
    void testUpdateRideStatus_MissingStatus_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long rideId = 100L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(null) // Invalid: null status
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).updateRideStatus(anyLong(), any());
    }

    @Test
    void testUpdateRideStatus_RideNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long rideId = 999L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(RideStatus.STARTED)
                .build();

        when(rideService.updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class)))
                .thenThrow(new RideNotFoundException("Ride not found: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(rideService, times(1)).updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class));
    }

    @Test
    void testGetRideInternal_Success_ShouldReturnRideInternalDTO() throws Exception {
        // Arrange
        Long rideId = 100L;
        Ride ride = Ride.builder()
                .rideId(rideId)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.REQUESTED)
                .build();

        when(rideService.getRideEntity(rideId)).thenReturn(ride);

        // Act & Assert
        mockMvc.perform(get("/api/v1/internal/rides/{rideId}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.riderId").value(1L));

        verify(rideService).getRideEntity(rideId);
    }

    @Test
    void testGetRideInternal_NotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long rideId = 999L;
        when(rideService.getRideEntity(rideId))
                .thenThrow(new RideNotFoundException("Ride not found: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/internal/rides/{rideId}", rideId))
                .andExpect(status().isNotFound());

        verify(rideService).getRideEntity(rideId);
    }

    @Test
    void testUpdateRideStatus_ToCancelled_ShouldReturnOk() throws Exception {
        // Arrange
        Long rideId = 100L;
        UpdateRideStatusRequest request = UpdateRideStatusRequest.builder()
                .status(RideStatus.CANCELLED)
                .build();

        RideDTO rideDTO = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .status(RideStatus.CANCELLED)
                .cancelledAt(LocalDateTime.now())
                .cancellationReason("Driver cancelled")
                .build();

        when(rideService.updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class))).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/internal/rides/{rideId}/status", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(rideService, times(1)).updateRideStatus(anyLong(), any(UpdateRideStatusRequest.class));
    }
}
