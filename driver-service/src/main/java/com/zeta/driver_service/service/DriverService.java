package com.zeta.driver_service.service;

import com.zeta.driver_service.dto.CreateDriverRequest;
import com.zeta.driver_service.dto.DriverDTO;
import com.zeta.driver_service.entity.Driver;
import com.zeta.driver_service.entity.DriverAvailability;
import com.zeta.driver_service.entity.DriverLocation;
import com.zeta.driver_service.enums.DriverStatus;
import com.zeta.driver_service.exception.DriverNotFoundException;
import com.zeta.driver_service.exception.DuplicateEmailException;
import com.zeta.driver_service.repository.DriverAvailabilityRepository;
import com.zeta.driver_service.repository.DriverLocationRepository;
import com.zeta.driver_service.repository.DriverRepository;
import com.zeta.driver_service.util.DriverMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final DriverAvailabilityRepository driverAvailabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public DriverDTO createDriver(CreateDriverRequest request) {
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        if (driverRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateEmailException("Phone already registered");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateEmailException("License number already registered");
        }

        Driver driver = Driver.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .licenseNumber(request.getLicenseNumber())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .status(DriverStatus.ACTIVE)
                .totalRides(0)
                .build();

        Driver savedDriver = driverRepository.save(driver);

        DriverAvailability availability = DriverAvailability.builder()
                .driverId(savedDriver.getDriverId())
                .available(false)
                .build();

        driverAvailabilityRepository.save(availability);

        log.info("Driver created: {}", savedDriver.getDriverId());
        return DriverMapper.toDTO(savedDriver, null, false);
    }

    @Transactional(readOnly = true)
    public DriverDTO getDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        DriverLocation location = driverLocationRepository.findByDriverId(driverId).orElse(null);
        DriverAvailability availability = driverAvailabilityRepository.findByDriverId(driverId).orElse(null);

        return DriverMapper.toDTO(driver, location, availability != null && availability.isAvailable());
    }

    @Transactional
    public void incrementTotalRides(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        driver.setTotalRides(driver.getTotalRides() + 1);
        driverRepository.save(driver);
    }
    @Transactional
    public Driver updateLocation(Long driverId, double latitude, double longitude) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        DriverLocation location = driverLocationRepository
                .findByDriverId(driverId)
                .orElseGet(() -> DriverLocation.builder()
                        .driverId(driverId)
                        .build());

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        driverLocationRepository.save(location);

        return driver;
    }

}
