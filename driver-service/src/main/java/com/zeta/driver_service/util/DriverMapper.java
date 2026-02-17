package com.zeta.driver_service.util;

import com.zeta.driver_service.dto.DriverDTO;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.entity.DriverLocation;

public class DriverMapper {

    public static DriverDTO toDTO(Driver driver, DriverLocation location, Boolean available) {
        if (driver == null) {
            return null;
        }

        return DriverDTO.builder()
                .driverId(driver.getDriverId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleNumber(driver.getVehicleNumber())
                .vehicleType(driver.getVehicleType())
                .totalRides(driver.getTotalRides())
                .latitude(location != null ? location.getLatitude() : null)
                .longitude(location != null ? location.getLongitude() : null)
                .available(available != null ? available : false)
                .build();
    }
}