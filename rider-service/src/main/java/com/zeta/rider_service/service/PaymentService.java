package com.zeta.rider_service.service;

import com.zeta.rider_service.dto.PaymentDTO;
import com.zeta.rider_service.dto.ProcessPaymentRequest;
import com.zeta.rider_service.entity.Payment;
import com.zeta.rider_service.entity.Ride;
import com.zeta.rider_service.enums.PaymentStatus;
import com.zeta.rider_service.exception.PaymentNotFoundException;
import com.zeta.rider_service.exception.PaymentProcessingException;
import com.zeta.rider_service.exception.RideNotFoundException;
import com.zeta.rider_service.repository.PaymentRepository;
import com.zeta.rider_service.repository.RideRepository;
import com.zeta.rider_service.util.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    @Transactional
    public PaymentDTO processPayment(ProcessPaymentRequest request) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + request.getRideId()));

        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .riderId(ride.getRiderId())
                .amount(ride.getFareAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        try {
            String transactionId = UUID.randomUUID().toString();
            payment.setTransactionId(transactionId);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);
            log.info("Payment processed for ride {}", request.getRideId());

            return PaymentMapper.toDTO(saved);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Payment failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByRide(Long rideId) {
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ride: " + rideId));
        return PaymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByRider(Long riderId) {
        return paymentRepository.findByRiderId(riderId)
                .stream()
                .map(PaymentMapper::toDTO)
                .toList();
    }
}
