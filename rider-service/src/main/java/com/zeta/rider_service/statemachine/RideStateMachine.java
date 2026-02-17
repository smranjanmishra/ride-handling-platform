package com.zeta.rider_service.statemachine;

import com.zeta.rider_service.enums.RideStatus;
import com.zeta.rider_service.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RideStateMachine {
    private static final Map<RideStatus, Set<RideStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(RideStatus.REQUESTED,
                Set.of(RideStatus.DRIVER_ASSIGNED, RideStatus.CANCELLED, RideStatus.NO_DRIVER_AVAILABLE));

        VALID_TRANSITIONS.put(RideStatus.DRIVER_ASSIGNED,
                Set.of(RideStatus.DRIVER_ARRIVED, RideStatus.STARTED, RideStatus.CANCELLED));

        VALID_TRANSITIONS.put(RideStatus.DRIVER_ARRIVED,
                Set.of(RideStatus.STARTED, RideStatus.CANCELLED));

        VALID_TRANSITIONS.put(RideStatus.STARTED,
                Set.of(RideStatus.COMPLETED));

        VALID_TRANSITIONS.put(RideStatus.NO_DRIVER_AVAILABLE,
                Set.of(RideStatus.REQUESTED));

        VALID_TRANSITIONS.put(RideStatus.COMPLETED, Collections.emptySet());
        VALID_TRANSITIONS.put(RideStatus.CANCELLED, Collections.emptySet());
    }

    public void validateTransition(RideStatus currentStatus, RideStatus newStatus) {
        Set<RideStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);

        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Invalid state transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public RideStatus validateAndGetNextStatus(RideStatus current, RideStatus requested) {
        validateTransition(current, requested);
        return requested;
    }
}


