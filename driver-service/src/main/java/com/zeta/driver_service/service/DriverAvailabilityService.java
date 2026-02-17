package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.DriverDTO;
import com.zeta.driver_service.dto.UpdateAvailabilityRequest;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.entity.DriverAvailability;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
import com.zeta.driver_service.util.DistanceCalculator;
import com.zeta.driver_service.util.DriverMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverAvailabilityService {
    private final DriverAvailabilityRepository driverAvailabilityRepository;
    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final DistanceCalculator distanceCalculator;
    private final AuthorizationService authorizationService;
    private final MetricsService metricsService;

    @Transactional
    public boolean updateAvailability(Long driverId, UpdateAvailabilityRequest request) {
        DriverAvailability availability = driverAvailabilityRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        availability.setAvailable(request.isAvailable());
        driverAvailabilityRepository.save(availability);

        return availability.isAvailable();
    }


    @Transactional(readOnly = true)
    public List<DriverDTO> getAvailableDrivers(Double latitude, Double longitude, Double radiusKm) {
        List<DriverAvailability> availableDrivers = driverAvailabilityRepository.findByAvailable(true);
        List<DriverDTO> nearbyDrivers = new ArrayList<>();

        for (DriverAvailability availability : availableDrivers) {
            DriverLocation location = driverLocationRepository.findByDriverId(availability.getDriverId())
                    .orElse(null);

            if (location != null) {
                double distance = distanceCalculator.calculateDistance(
                        latitude, longitude,
                        location.getLatitude(), location.getLongitude()
                );

                if (distance <= radiusKm) {
                    Driver driver = driverRepository.findById(availability.getDriverId())
                            .orElse(null);
                    if (driver != null) {
                        nearbyDrivers.add(DriverMapper.toDTO(driver, location, true));
                    }
                }
            }
        }

        log.info("Found {} available drivers within {}km", nearbyDrivers.size(), radiusKm);
        return nearbyDrivers;
    }

    @Transactional
    public void markDriverBusy(Long driverId, Long rideId) {
        DriverAvailability availability = driverAvailabilityRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver availability not found: " + driverId));

        availability.setAvailable(false);
        availability.setCurrentRideId(rideId);
        driverAvailabilityRepository.save(availability);

        log.info("Marked driver {} as busy with ride {}", driverId, rideId);
    }

    @Transactional
    public void markDriverAvailable(Long driverId) {
        DriverAvailability availability = driverAvailabilityRepository.findByDriverId(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver availability not found: " + driverId));

        availability.setAvailable(true);
        availability.setCurrentRideId(null);
        driverAvailabilityRepository.save(availability);

        log.info("Marked driver {} as available", driverId);
    }
}
