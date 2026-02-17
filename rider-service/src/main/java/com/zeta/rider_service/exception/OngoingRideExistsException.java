package com.zeta.rider_service.exception;

public class OngoingRideExistsException extends RuntimeException {
    public OngoingRideExistsException(String message) {
        super(message);
    }
}
