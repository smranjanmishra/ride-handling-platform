package com.zeta.rider_service.client;

import com.zeta.rider_service.dto.DriverDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriverServiceClient {
    private final RestClient restClient;

    @Value("${services.driver-service.url}")
    private String driverServiceUrl;

    public List<DriverDTO> getAvailableDrivers(Double latitude, Double longitude, Double radiusKm) {
        String url = String.format("%s/api/v1/drivers/available?latitude=%s&longitude=%s&radiusKm=%s",
                driverServiceUrl, latitude, longitude, radiusKm);
        System.out.println("url = " + url);

        log.info("Fetching available drivers from: {}", url);

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DriverDTO>>() {});
    }
    public void notifyDriver(Long driverId, Long rideId) {
        String url = String.format(
                "%s/api/v1/drivers/%s/notify-ride/%s",
                driverServiceUrl, driverId, rideId
        );
        System.out.println("url = " + url);

        log.info("Notifying driver {} about ride {}", driverId, rideId);

        restClient.post()
                .uri(url)
                .header("Authorization",
                        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlclR5cGUiOiJEUklWRVIiLCJpYXQiOjE3NjgzOTc3NzgsImV4cCI6MTc2ODQ4NDE3OH0.TqgU0ZnW-p3ppSlP5AaiHZK5EhCWYCk4Fc-eb3dAols")
                .retrieve()
                .toBodilessEntity();
    }

}