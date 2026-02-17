package com.zeta.rider_service.dto;

import com.zeta.rider_service.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    @NotNull(message = "Ride ID is required")
    private Long rideId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
