package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.UpdateLocationRequest;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLocationService {
    private final DriverLocationRepository driverLocationRepository;
    private final DriverRepository driverRepository;
    private final AuthorizationService authorizationService;

    @Transactional
    public void updateLocation(Long driverId, UpdateLocationRequest request) {
        authorizationService.verifyDriverOwnership(driverId);

        if (!driverRepository.existsById(driverId)) {
            throw new DriverNotFoundException("Driver not found: " + driverId);
        }

        DriverLocation location = driverLocationRepository.findByDriverId(driverId)
                .orElse(DriverLocation.builder().driverId(driverId).build());

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());

        driverLocationRepository.save(location);
        log.info("Updated location for driver {}", driverId);
    }
}
