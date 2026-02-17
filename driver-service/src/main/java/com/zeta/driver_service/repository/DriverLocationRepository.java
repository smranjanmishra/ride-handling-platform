package com.zeta.driver_service.repository;

import com.zeta.driver_service.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    Optional<DriverLocation> findByDriverId(Long driverId);
    void deleteByDriverId(Long driverId);
}
