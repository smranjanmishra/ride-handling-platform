package com.zeta.driver_service.client;

import com.zeta.driver_service.dto.UpdateRideStatusRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiderServiceClient {

    private final RestClient restClient;

    @Value("${services.rider-service.url}")
    private String riderServiceUrl;

    public void updateRideStatus(Long rideId, UpdateRideStatusRequest request) {
        String url = String.format("%s/api/v1/internal/rides/%s/status", riderServiceUrl, rideId);
        System.out.println(request);

        log.info("Updating ride {} status to {}", rideId, request.getStatus());

        restClient.put()
                .uri(url)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
