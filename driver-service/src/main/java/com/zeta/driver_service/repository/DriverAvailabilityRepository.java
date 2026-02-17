package com.zeta.driver_service.repository;

import com.zeta.driver_service.entity.DriverAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverAvailabilityRepository extends JpaRepository<DriverAvailability, Long> {
    Optional<DriverAvailability> findByDriverId(Long driverId);
    List<DriverAvailability> findByAvailable(Boolean available);
}
