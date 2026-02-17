package com.zeta.rider_service.dto;

import com.zeta.rider_service.enums.RiderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderDTO {
    private Long riderId;
    private String name;
    private String email;
    private String phone;
    private Integer totalRides;
    private RiderStatus status;
    private LocalDateTime createdAt;
}
