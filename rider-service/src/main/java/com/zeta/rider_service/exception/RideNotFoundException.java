package com.zeta.rider_service.exception;

public class RideNotFoundException  extends RuntimeException{
    public RideNotFoundException(String message) {
        super(message);
    }
}
