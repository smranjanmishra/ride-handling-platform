package com.zeta.rider_service.repository;

import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRiderIdOrderByRequestedAtDesc(Long riderId);
    boolean existsByRiderIdAndStatusIn(Long riderId, List<RideStatus> statuses);
    boolean existsByIdempotencyKey(String idempotencyKey);
    Ride findByIdempotencyKey(String idempotencyKey);
}
