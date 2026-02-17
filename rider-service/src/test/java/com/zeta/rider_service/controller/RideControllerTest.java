package com.zeta.rider_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeta.rider_service.dto.*;
import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.GlobalExceptionHandler;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.service.RideService;
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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

    @Mock
    private RideService rideService;

    @InjectMocks
    private RideController rideController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateRide_Success_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateRideRequest request = CreateRideRequest.builder()
                .riderId(1L)
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .pickupAddress("123 Main St")
                .dropLatitude(40.7589)
                .dropLongitude(-73.9851)
                .dropAddress("456 Park Ave")
                .build();

        RideDTO rideDTO = RideDTO.builder()
                .rideId(100L)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.REQUESTED)
                .fareAmount(BigDecimal.valueOf(60.0))
                .distanceKm(1.0)
                .requestedAt(LocalDateTime.now())
                .build();

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rideId").value(100L))
                .andExpect(jsonPath("$.riderId").value(1L))
                .andExpect(jsonPath("$.driverId").value(2L))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        verify(rideService).createRide(any(CreateRideRequest.class));
    }

    @Test
    void testCreateRide_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateRideRequest request = CreateRideRequest.builder()
                .riderId(null) // Invalid: null riderId
                .pickupLatitude(40.7128)
                .pickupLongitude(-74.0060)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).createRide(any());
    }

    @Test
    void testGetRide_Success_ShouldReturnRide() throws Exception {
        // Arrange
        Long rideId = 100L;
        RideDTO rideDTO = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .driverId(2L)
                .status(RideStatus.REQUESTED)
                .fareAmount(BigDecimal.valueOf(60.0))
                .build();

        when(rideService.getRide(rideId)).thenReturn(rideDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/{rideId}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.riderId").value(1L))
                .andExpect(jsonPath("$.driverId").value(2L));

        verify(rideService).getRide(rideId);
    }

    @Test
    void testGetRide_NotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long rideId = 999L;
        when(rideService.getRide(rideId)).thenThrow(new RideNotFoundException("Ride not found: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/{rideId}", rideId))
                .andExpect(status().isNotFound());

        verify(rideService).getRide(rideId);
    }

    @Test
    void testCancelRide_Success_ShouldReturnOk() throws Exception {
        // Arrange
        Long rideId = 100L;
        CancelRideRequest cancelRequest = CancelRideRequest.builder()
                .cancellationReason("Changed my mind")
                .build();

        RideDTO cancelledRide = RideDTO.builder()
                .rideId(rideId)
                .riderId(1L)
                .status(RideStatus.CANCELLED)
                .cancellationReason("Changed my mind")
                .cancelledAt(LocalDateTime.now())
                .build();

        when(rideService.cancelRide(rideId, cancelRequest)).thenReturn(cancelledRide);

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/{rideId}/cancel", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(rideId))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Changed my mind"));

        verify(rideService).cancelRide(rideId, cancelRequest);
    }

    @Test
    void testCancelRide_MissingReason_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Long rideId = 100L;
        CancelRideRequest cancelRequest = CancelRideRequest.builder()
                .cancellationReason("") // Invalid: empty reason
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/rides/{rideId}/cancel", rideId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).cancelRide(anyLong(), any());
    }

    @Test
    void testGetNearbyCabs_Success_ShouldReturnDriverList() throws Exception {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double radiusKm = 5.0;

        DriverDTO driver1 = DriverDTO.builder()
                .driverId(1L)
                .name("Driver One")
                .vehicleNumber("ABC123")
                .vehicleType("Sedan")
                .latitude(40.7150)
                .longitude(-74.0050)
                .available(true)
                .build();

        DriverDTO driver2 = DriverDTO.builder()
                .driverId(2L)
                .name("Driver Two")
                .vehicleNumber("XYZ789")
                .vehicleType("SUV")
                .latitude(40.7100)
                .longitude(-74.0070)
                .available(true)
                .build();

        List<DriverDTO> drivers = Arrays.asList(driver1, driver2);
        when(rideService.getNearbyCabs(latitude, longitude, radiusKm)).thenReturn(drivers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/nearby-cabs")
                        .param("latitude", latitude.toString())
                        .param("longitude", longitude.toString())
                        .param("radiusKm", radiusKm.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].driverId").value(1L))
                .andExpect(jsonPath("$[0].name").value("Driver One"))
                .andExpect(jsonPath("$[1].driverId").value(2L));

        verify(rideService).getNearbyCabs(latitude, longitude, radiusKm);
    }

    @Test
    void testGetNearbyCabs_WithDefaultRadius_ShouldUseDefault() throws Exception {
        // Arrange
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        Double defaultRadius = 5.0; // Default value

        List<DriverDTO> drivers = Arrays.asList();
        when(rideService.getNearbyCabs(latitude, longitude, defaultRadius)).thenReturn(drivers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/nearby-cabs")
                        .param("latitude", latitude.toString())
                        .param("longitude", longitude.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rideService).getNearbyCabs(latitude, longitude, defaultRadius);
    }

    @Test
    void testGetNearbyCabs_MissingLatitude_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/nearby-cabs")
                        .param("longitude", "-74.0060"))
                .andExpect(status().isBadRequest());

        verify(rideService, never()).getNearbyCabs(anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void testGetRidesByRider_Success_ShouldReturnRideList() throws Exception {
        // Arrange
        Long riderId = 1L;
        RideDTO ride1 = RideDTO.builder()
                .rideId(100L)
                .riderId(riderId)
                .status(RideStatus.COMPLETED)
                .build();

        RideDTO ride2 = RideDTO.builder()
                .rideId(101L)
                .riderId(riderId)
                .status(RideStatus.REQUESTED)
                .build();

        List<RideDTO> rides = Arrays.asList(ride1, ride2);
        when(rideService.getRidesByRider(riderId)).thenReturn(rides);

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/rider/{riderId}", riderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rideId").value(100L))
                .andExpect(jsonPath("$[1].rideId").value(101L));

        verify(rideService).getRidesByRider(riderId);
    }

    @Test
    void testGetRidesByRider_EmptyList_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        Long riderId = 1L;
        when(rideService.getRidesByRider(riderId)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/rides/rider/{riderId}", riderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rideService).getRidesByRider(riderId);
    }
}
