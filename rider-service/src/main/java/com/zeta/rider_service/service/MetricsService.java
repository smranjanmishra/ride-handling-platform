package com.zeta.rider_service.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MetricsService {
    private final Counter ridesCreated;
    private final Counter ridesCancelled;
    private final Counter ridesCompleted;
    private final Counter assignmentsFailed;
    private final Counter validationFailures;
    private final Timer rideCreationTimer;
    private final Timer assignmentTimer;

    public MetricsService(MeterRegistry registry) {
        this.ridesCreated = Counter.builder("rides.created")
                .description("Total number of rides created")
                .register(registry);

        this.ridesCancelled = Counter.builder("rides.cancelled")
                .description("Total number of rides cancelled")
                .register(registry);

        this.ridesCompleted = Counter.builder("rides.completed")
                .description("Total number of rides completed")
                .register(registry);

        this.assignmentsFailed = Counter.builder("assignments.failed")
                .description("Total number of failed driver assignments")
                .register(registry);

        this.validationFailures = Counter.builder("validation.failures")
                .description("Total number of validation failures")
                .register(registry);

        this.rideCreationTimer = Timer.builder("ride.creation.duration")
                .description("Time taken to create a ride")
                .register(registry);

        this.assignmentTimer = Timer.builder("driver.assignment.duration")
                .description("Time taken to assign a driver")
                .register(registry);
    }

    public void incrementRidesCreated() {
        ridesCreated.increment();
        log.debug("Ride created metric incremented");
    }

    public void incrementRidesCancelled() {
        ridesCancelled.increment();
        log.debug("Ride cancelled metric incremented");
    }

    public void incrementRidesCompleted() {
        ridesCompleted.increment();
        log.debug("Ride completed metric incremented");
    }

    public void incrementAssignmentsFailed() {
        assignmentsFailed.increment();
        log.warn("Assignment failed metric incremented");
    }

    public void incrementValidationFailures() {
        validationFailures.increment();
        log.debug("Validation failure metric incremented");
    }

    public void recordRideCreationTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        rideCreationTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    public void recordAssignmentTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        assignmentTimer.record(duration, TimeUnit.MILLISECONDS);
    }
}
