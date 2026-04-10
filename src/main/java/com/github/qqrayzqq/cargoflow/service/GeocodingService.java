package com.github.qqrayzqq.cargoflow.service;

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

    private final RestClient restClient;

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

    /*
     * возвращает координаты [latitude, longitude] или null если не найдено.
     */
    public double[] geocode(String address) {
        log.debug("Geocoding request for: {}", address);
        try {
            List<Map<String, Object>> results = restClient.get()
                    .uri("/search?q={address}&format=json&limit=1", address)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            log.debug("Geocoding results: {}", results);
            if (results == null || results.isEmpty()) return null;

            double lat = Double.parseDouble((String) results.getFirst().get("lat"));
            double lon = Double.parseDouble((String) results.getFirst().get("lon"));
            return new double[]{lat, lon};
        } catch (RestClientException e) {
            log.warn("Geocoding failed for address '{}': {}", address, e.getMessage());
            return null;
        }
    }
}
