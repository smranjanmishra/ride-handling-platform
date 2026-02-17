package com.zeta.rider_service.service;

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
        // Use SimpleMeterRegistry for testing - it's designed for unit tests
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void testIncrementRidesCreated_ShouldIncrementCounter() {
        // Act
        metricsService.incrementRidesCreated();

        // Assert
        Counter counter = meterRegistry.counter("rides.created");
        assertEquals(1.0, counter.count());
    }

    @Test
    void testIncrementRidesCancelled_ShouldIncrementCounter() {
        // Act
        metricsService.incrementRidesCancelled();

        // Assert
        Counter counter = meterRegistry.counter("rides.cancelled");
        assertEquals(1.0, counter.count());
    }

    @Test
    void testIncrementRidesCompleted_ShouldIncrementCounter() {
        // Act
        metricsService.incrementRidesCompleted();

        // Assert
        Counter counter = meterRegistry.counter("rides.completed");
        assertEquals(1.0, counter.count());
    }

    @Test
    void testIncrementAssignmentsFailed_ShouldIncrementCounter() {
        // Act
        metricsService.incrementAssignmentsFailed();

        // Assert
        Counter counter = meterRegistry.counter("assignments.failed");
        assertEquals(1.0, counter.count());
    }

    @Test
    void testIncrementValidationFailures_ShouldIncrementCounter() {
        // Act
        metricsService.incrementValidationFailures();

        // Assert
        Counter counter = meterRegistry.counter("validation.failures");
        assertEquals(1.0, counter.count());
    }

    @Test
    void testRecordRideCreationTime_ShouldRecordTimer() {
        // Arrange
        long startTime = System.currentTimeMillis() - 100; // 100ms ago

        // Act
        metricsService.recordRideCreationTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("ride.creation.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 90);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 150);
    }

    @Test
    void testRecordRideCreationTime_CalculatesCorrectDuration() {
        // Arrange
        long startTime = System.currentTimeMillis() - 150; // 150ms ago

        // Act
        metricsService.recordRideCreationTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("ride.creation.duration").timer();
        assertNotNull(timer);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 100);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 200);
    }

    @Test
    void testRecordAssignmentTime_ShouldRecordTimer() {
        // Arrange
        long startTime = System.currentTimeMillis() - 50; // 50ms ago

        // Act
        metricsService.recordAssignmentTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("driver.assignment.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 40);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 80);
    }

    @Test
    void testRecordAssignmentTime_CalculatesCorrectDuration() {
        // Arrange
        long startTime = System.currentTimeMillis() - 75; // 75ms ago

        // Act
        metricsService.recordAssignmentTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("driver.assignment.duration").timer();
        assertNotNull(timer);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 50);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 100);
    }

    @Test
    void testMultipleIncrements_ShouldCallCounterMultipleTimes() {
        // Act
        metricsService.incrementRidesCreated();
        metricsService.incrementRidesCreated();
        metricsService.incrementRidesCreated();

        // Assert
        Counter counter = meterRegistry.counter("rides.created");
        assertEquals(3.0, counter.count());
    }

    @Test
    void testAllCounters_ShouldBeInitialized() {
        // Act - Call all increment methods
        metricsService.incrementRidesCreated();
        metricsService.incrementRidesCancelled();
        metricsService.incrementRidesCompleted();
        metricsService.incrementAssignmentsFailed();
        metricsService.incrementValidationFailures();

        // Assert - Verify all counters were incremented
        assertEquals(1.0, meterRegistry.counter("rides.created").count());
        assertEquals(1.0, meterRegistry.counter("rides.cancelled").count());
        assertEquals(1.0, meterRegistry.counter("rides.completed").count());
        assertEquals(1.0, meterRegistry.counter("assignments.failed").count());
        assertEquals(1.0, meterRegistry.counter("validation.failures").count());
    }

    @Test
    void testRecordRideCreationTime_WithZeroDuration() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        metricsService.recordRideCreationTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("ride.creation.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 0);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) < 100);
    }

    @Test
    void testRecordAssignmentTime_WithZeroDuration() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        metricsService.recordAssignmentTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("driver.assignment.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 0);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) < 100);
    }

    @Test
    void testRecordRideCreationTime_WithLargeDuration() {
        // Arrange
        long startTime = System.currentTimeMillis() - 5000; // 5 seconds ago

        // Act
        metricsService.recordRideCreationTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("ride.creation.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 4000);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 6000);
    }

    @Test
    void testRecordAssignmentTime_WithLargeDuration() {
        // Arrange
        long startTime = System.currentTimeMillis() - 3000; // 3 seconds ago

        // Act
        metricsService.recordAssignmentTime(startTime);

        // Assert
        Timer timer = meterRegistry.find("driver.assignment.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 2500);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) <= 3500);
    }

    @Test
    void testMultipleTimerRecordings_ShouldAccumulate() {
        // Arrange
        long startTime1 = System.currentTimeMillis() - 100;
        long startTime2 = System.currentTimeMillis() - 50;

        // Act
        metricsService.recordRideCreationTime(startTime1);
        metricsService.recordRideCreationTime(startTime2);

        // Assert
        Timer timer = meterRegistry.find("ride.creation.duration").timer();
        assertNotNull(timer);
        assertEquals(2, timer.count());
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) >= 100);
    }
}
