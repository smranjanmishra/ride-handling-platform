package com.zeta.rider_service.repository;

import com.zeta.rider_service.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {
    Optional<Rider> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
