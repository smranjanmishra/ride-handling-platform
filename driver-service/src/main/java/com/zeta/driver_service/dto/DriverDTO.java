package com.zeta.driver_service.dto;

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
    private String email;
    private String phone;
    private String licenseNumber;
    private String vehicleNumber;
    private String vehicleType;
    private Integer totalRides;
    private Double latitude;
    private Double longitude;
    private Boolean available;
}