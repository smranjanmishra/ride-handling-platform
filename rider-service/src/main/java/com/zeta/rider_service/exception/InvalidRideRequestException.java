package com.zeta.rider_service.exception;

public class InvalidRideRequestException extends RuntimeException {
    public InvalidRideRequestException(String message) {
        super(message);
    }
}
