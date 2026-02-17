package com.zeta.driver_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MetricsService {
    private final Counter driversRegistered;
    private final Counter ridesAccepted;
    private final Counter ridesCompleted;
    private final Counter ridesRejected;
    private final Counter availabilityChanges;
    private final Timer rideAcceptanceTimer;

    public MetricsService(MeterRegistry registry) {
        this.driversRegistered = Counter.builder("drivers.registered")
                .description("Total number of drivers registered")
                .register(registry);

        this.ridesAccepted = Counter.builder("rides.accepted")
                .description("Total number of rides accepted by drivers")
                .register(registry);

        this.ridesCompleted = Counter.builder("rides.completed.by.driver")
                .description("Total number of rides completed by drivers")
                .register(registry);

        this.ridesRejected = Counter.builder("rides.rejected")
                .description("Total number of rides rejected")
                .register(registry);

        this.availabilityChanges = Counter.builder("driver.availability.changes")
                .description("Total number of driver availability changes")
                .register(registry);

        this.rideAcceptanceTimer = Timer.builder("ride.acceptance.duration")
                .description("Time taken for driver to accept ride")
                .register(registry);
    }

    public void incrementDriversRegistered() {
        driversRegistered.increment();
        log.debug("Driver registered metric incremented");
    }

    public void incrementRidesAccepted() {
        ridesAccepted.increment();
        log.debug("Ride accepted metric incremented");
    }

    public void incrementRidesCompleted() {
        ridesCompleted.increment();
        log.debug("Ride completed metric incremented");
    }

    public void incrementRidesRejected() {
        ridesRejected.increment();
        log.debug("Ride rejected metric incremented");
    }

    public void incrementAvailabilityChanges() {
        availabilityChanges.increment();
        log.debug("Availability change metric incremented");
    }

    public void recordRideAcceptanceTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        rideAcceptanceTimer.record(duration, TimeUnit.MILLISECONDS);
    }
}
