package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.CreateRiderRequest;
import com.zeta.rider_service.dto.RiderDTO;
import com.zeta.rider_service.entity.Rider;
import com.zeta.rider_service.enums.RiderStatus;
import com.zeta.rider_service.exception.DuplicateEmailException;
import com.zeta.rider_service.exception.DuplicatePhoneException;
import com.zeta.rider_service.exception.RiderNotFoundException;
import com.zeta.rider_service.repository.RiderRepository;
import com.zeta.rider_service.util.mapper.RiderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderService {
    private final RiderRepository riderRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RiderDTO createRider(CreateRiderRequest request) {
        if (riderRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        if (riderRepository.existsByPhone(request.getPhone())) {
            throw new DuplicatePhoneException("Phone already registered");
        }

        Rider rider = Rider.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(RiderStatus.ACTIVE)
                .totalRides(0)
                .build();

        Rider saved = riderRepository.save(rider);
        log.info("Rider created: {}", saved.getRiderId());

        return RiderMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public RiderDTO getRider(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RiderNotFoundException("Rider not found: " + riderId));
        return RiderMapper.toDTO(rider);
    }

    @Transactional
    public void incrementTotalRides(Long riderId) {
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RiderNotFoundException("Rider not found: " + riderId));
        rider.setTotalRides(rider.getTotalRides() + 1);
        riderRepository.save(rider);
        log.info("Incremented total rides for rider {}", riderId);
    }
}
