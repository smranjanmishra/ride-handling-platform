package com.zeta.driver_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRideStatusRequest {
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
