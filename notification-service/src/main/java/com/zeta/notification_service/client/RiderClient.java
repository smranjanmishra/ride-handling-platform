package com.zeta.notification_service.client;

import com.zeta.notification_service.entity.RideDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RiderClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public RideDetails getRide(Long rideId) {
        return restTemplate.getForObject(
                "http://localhost:8081/api/v1/internal/rides/" + rideId,
                RideDetails.class
        );
    }
}
