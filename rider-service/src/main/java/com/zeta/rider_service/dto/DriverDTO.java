package com.zeta.rider_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDTO {
    private Long driverId;
    private String name;
    private String vehicleNumber;
    private String vehicleType;
    private Double latitude;
    private Double longitude;
    private Boolean available;
}
