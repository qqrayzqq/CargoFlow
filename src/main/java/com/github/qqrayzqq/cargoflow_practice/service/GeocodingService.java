package com.github.qqrayzqq.cargoflow_practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeocodingService {

    private final RestClient restClient;

    public GeocodingService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "cargoflow-app")
                .build();
    }

    /*
     * возвращает координаты [latitude, longitude] или null если не найдено.
     */
    public double[] geocode(String address) {
        log.debug("Geocoding request for: {}", address);
        List<Map<String, Object>> results = restClient.get()
                .uri("/search?q={address}&format=json&limit=1", address)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        log.debug("Geocoding results: {}", results);

        if(results == null || results.isEmpty()) return null;

        double lat = Double.parseDouble((String) results.getFirst().get("lat"));
        double lon = Double.parseDouble((String) results.getFirst().get("lon"));
        return new double[]{lat, lon};
    }
}
