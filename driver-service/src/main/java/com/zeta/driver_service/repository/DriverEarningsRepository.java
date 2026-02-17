package com.zeta.driver_service.repository;

import com.zeta.driver_service.entity.DriverEarnings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverEarningsRepository extends JpaRepository<DriverEarnings, Long> {
    List<DriverEarnings> findByDriverId(Long driverId);
}
