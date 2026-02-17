package com.zeta.rider_service.controller;

import com.zeta.rider_service.dto.PaymentDTO;
import com.zeta.rider_service.dto.ProcessPaymentRequest;
import com.zeta.rider_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentDTO> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        log.info("REST request to process payment for ride: {}", request.getRideId());
        PaymentDTO payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<PaymentDTO> getPaymentByRide(
            @PathVariable("rideId") Long rideId) {
        log.info("REST request to get payment for ride: {}", rideId);
        PaymentDTO payment = paymentService.getPaymentByRide(rideId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/rider/{riderId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByRider(
            @PathVariable("riderId") Long riderId) {
        log.info("REST request to get payments for rider: {}", riderId);
        List<PaymentDTO> payments = paymentService.getPaymentsByRider(riderId);
        return ResponseEntity.ok(payments);
    }
}
