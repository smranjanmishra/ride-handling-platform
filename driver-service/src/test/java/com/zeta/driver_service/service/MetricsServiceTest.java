package com.zeta.driver_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void testIncrementDriversRegistered_IncrementsCounter() {
        // Arrange
        Counter counter = meterRegistry.find("drivers.registered").counter();
        double initialCount = counter.count();

        // Act
        metricsService.incrementDriversRegistered();

        // Assert
        assertEquals(initialCount + 1, counter.count(), 0.001);
    }

    @Test
    void testIncrementRidesAccepted_IncrementsCounter() {
        // Arrange
        Counter counter = meterRegistry.find("rides.accepted").counter();
        double initialCount = counter.count();

        // Act
        metricsService.incrementRidesAccepted();

        // Assert
        assertEquals(initialCount + 1, counter.count(), 0.001);
    }

    @Test
    void testIncrementRidesCompleted_IncrementsCounter() {
        // Arrange
        Counter counter = meterRegistry.find("rides.completed.by.driver").counter();
        double initialCount = counter.count();

        // Act
        metricsService.incrementRidesCompleted();

        // Assert
        assertEquals(initialCount + 1, counter.count(), 0.001);
    }

    @Test
    void testIncrementRidesRejected_IncrementsCounter() {
        // Arrange
        Counter counter = meterRegistry.find("rides.rejected").counter();
        double initialCount = counter.count();

        // Act
        metricsService.incrementRidesRejected();

        // Assert
        assertEquals(initialCount + 1, counter.count(), 0.001);
    }

    @Test
    void testIncrementAvailabilityChanges_IncrementsCounter() {
        // Arrange
        Counter counter = meterRegistry.find("driver.availability.changes").counter();
        double initialCount = counter.count();

        // Act
        metricsService.incrementAvailabilityChanges();

        // Assert
        assertEquals(initialCount + 1, counter.count(), 0.001);
    }

    @Test
    void testRecordRideAcceptanceTime_RecordsDuration() {
        // Arrange
        Timer timer = meterRegistry.find("ride.acceptance.duration").timer();
        long startTime = System.currentTimeMillis() - 1000; // 1 second ago
        long initialCount = timer.count();

        // Act
        metricsService.recordRideAcceptanceTime(startTime);

        // Assert
        assertEquals(initialCount + 1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 1000);
    }

    @Test
    void testRecordRideAcceptanceTime_CalculatesCorrectDuration() {
        // Arrange
        Timer timer = meterRegistry.find("ride.acceptance.duration").timer();
        long startTime = System.currentTimeMillis() - 500; // 500ms ago

        // Act
        metricsService.recordRideAcceptanceTime(startTime);

        // Assert
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 500);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) < 1000); // Should be close to 500ms
    }

    @Test
    void testMultipleIncrements_AllCountersWork() {
        // Act
        metricsService.incrementDriversRegistered();
        metricsService.incrementRidesAccepted();
        metricsService.incrementRidesCompleted();
        metricsService.incrementRidesRejected();
        metricsService.incrementAvailabilityChanges();

        // Assert
        assertEquals(1.0, meterRegistry.find("drivers.registered").counter().count(), 0.001);
        assertEquals(1.0, meterRegistry.find("rides.accepted").counter().count(), 0.001);
        assertEquals(1.0, meterRegistry.find("rides.completed.by.driver").counter().count(), 0.001);
        assertEquals(1.0, meterRegistry.find("rides.rejected").counter().count(), 0.001);
        assertEquals(1.0, meterRegistry.find("driver.availability.changes").counter().count(), 0.001);
    }

    @Test
    void testMultipleIncrements_SameCounter_IncrementsMultipleTimes() {
        // Act
        metricsService.incrementDriversRegistered();
        metricsService.incrementDriversRegistered();
        metricsService.incrementDriversRegistered();

        // Assert
        assertEquals(3.0, meterRegistry.find("drivers.registered").counter().count(), 0.001);
    }
}
