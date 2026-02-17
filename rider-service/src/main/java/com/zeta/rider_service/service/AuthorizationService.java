package com.zeta.rider_service.service;

import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.entity.Rider;
import com.zeta.rider_service.repository.RiderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

@Service
@Slf4j
public class AuthorizationService {
    private final RiderRepository riderRepository;

    public AuthorizationService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    public void verifyRideAccess(Ride ride) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String email = auth.getName();

        Rider rider = riderRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Rider not found"));

        boolean isRider = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RIDER"));
        boolean isDriver = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        if (isRider && !rider.getRiderId().equals(ride.getRiderId())) {
            log.warn("Rider {} attempted to access ride {} belonging to rider {}",
                    rider.getRiderId(), ride.getRideId(), ride.getRiderId());
            throw new AccessDeniedException("You can only access your own rides");
        }

        if (isDriver && ride.getDriverId() != null &&
                !rider.getRiderId().equals(ride.getDriverId())) {
            log.warn("Driver {} attempted to access ride {} assigned to driver {}",
                    rider.getRiderId(), ride.getRideId(), ride.getDriverId());
            throw new AccessDeniedException("You can only access rides assigned to you");
        }

        if (!isRider && !isDriver) {
            throw new AccessDeniedException("Invalid role");
        }
    }


    public void verifyRiderOwnership(Long riderIdFromRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailFromToken = authentication.getName(); // this is EMAIL

        Rider rider = riderRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new AccessDeniedException("Rider not found"));

        if (!rider.getRiderId().equals(riderIdFromRequest)) {
            throw new AccessDeniedException("You are not allowed to book ride for this rider");
        }
    }


    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String email = auth.getName();
        Rider rider = riderRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Rider not found"));

        return rider.getRiderId();
    }

    public boolean isDriver() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DRIVER"));
    }

    public boolean isRider() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RIDER"));
    }
}
