package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeocodingService {

    private static final long MIN_INTERVAL_MS = 1100;

    private final RestClient restClient;
    private long nextAllowedRequestTime = 0;

    public GeocodingService(@Value("${cargoflow.geocoding.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("User-Agent", "cargoflow-app")
                .build();
    }

    public double[] geocode(String address) {
        throttle();
        log.debug("Geocoding request for: {}", address);
        try {
            List<Map<String, Object>> results = restClient.get()
                    .uri("/search?q={address}&format=json&limit=1", address)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            log.debug("Geocoding results: {}", results);
            if (results == null || results.isEmpty()){
                throw new BadRequestException("Address not found: " + address);
            }

            double lat = Double.parseDouble((String) results.getFirst().get("lat"));
            double lon = Double.parseDouble((String) results.getFirst().get("lon"));
            return new double[]{lat, lon};
        } catch (RestClientException e) {
            log.warn("Geocoding failed for address '{}': {}", address, e.getMessage());
            throw new BadRequestException("Geocoding service unavailable, try again later");
        }
    }

    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long waitUntil = Math.max(now, nextAllowedRequestTime);
        nextAllowedRequestTime = waitUntil + MIN_INTERVAL_MS;

        long sleepTime = waitUntil - now;
        if (sleepTime > 0) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BadRequestException("Geocoding interrupted");
            }
        }
    }
}
