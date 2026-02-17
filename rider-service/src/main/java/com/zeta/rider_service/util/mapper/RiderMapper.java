package com.zeta.rider_service.util.mapper;

import com.zeta.rider_service.dto.RiderDTO;
import com.zeta.rider_service.entity.Rider;

public class RiderMapper {
    public static RiderDTO toDTO(Rider rider) {
        if (rider == null) {
            return null;
        }

        return RiderDTO.builder()
                .riderId(rider.getRiderId())
                .name(rider.getName())
                .email(rider.getEmail())
                .phone(rider.getPhone())
                .totalRides(rider.getTotalRides())
                .status(rider.getStatus())
                .createdAt(rider.getCreatedAt())
                .build();
    }
}
