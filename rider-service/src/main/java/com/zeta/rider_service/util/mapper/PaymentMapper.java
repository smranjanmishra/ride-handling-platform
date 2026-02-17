package com.zeta.rider_service.util.mapper;

import com.zeta.rider_service.dto.PaymentDTO;
import com.zeta.rider_service.entity.Payment;

public class PaymentMapper {
    public static PaymentDTO toDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDTO.builder()
                .paymentId(payment.getPaymentId())
                .rideId(payment.getRideId())
                .riderId(payment.getRiderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
