package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final BigDecimal RATE_PER_KG_KM = new BigDecimal("0.01");  // цена за кг*км
    private static final BigDecimal FRAGILE_MULTIPLIER = new BigDecimal("1.5");
    private static final double VOLUMETRIC_DIVISOR = 5000.0; // см³ → кг (стандарт авиа)

    public BigDecimal calculatePrice(Shipment shipment) {
        // 1. Получить shipment с адресами и посылками
        // 2. Проверить что у обоих адресов есть координаты
        // 3. Вызвать haversine() для расстояния
        // 4. Вызвать effectiveWeight() для каждой посылки, просуммировать
        // 5. Применить FRAGILE_MULTIPLIER если хотя бы одна посылка fragile
        // 6. price = totalWeight * distanceKm * RATE_PER_KG_KM
        if(shipment.getFromAddress().getLatitude() == null || shipment.getToAddress().getLatitude() == null) {
            throw new BadRequestException("Coordinates not found for this address");
        }
        double distance = haversine(shipment.getFromAddress().getLatitude(), shipment.getFromAddress().getLongitude(), shipment.getToAddress().getLatitude(), shipment.getToAddress().getLongitude());
        BigDecimal weight = BigDecimal.ZERO;
        boolean fragile = false;
        if(shipment.getParcels() != null) {
            for(Parcel parcel : shipment.getParcels()){
                if(parcel.isFragile()) fragile = true;
                weight = weight.add(effectiveWeight(parcel));
            }
        }
        log.debug("distance={}, totalWeight={}", distance, weight);
        BigDecimal price = weight.multiply(BigDecimal.valueOf(distance)).multiply(RATE_PER_KG_KM);
        if(fragile) price = price.multiply(FRAGILE_MULTIPLIER);
        return price;
    }

    /**
     * Расстояние между двумя точками на сфере в километрах (формула haversine).
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double lat = Math.toRadians(lat2 - lat1);
        double lon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(lat / 2), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(lon/2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    /**
     * Эффективный вес посылки: максимум из реального и объёмного.
     * Volumetric weight = (width * height * length) / VOLUMETRIC_DIVISOR
     */
    private BigDecimal effectiveWeight(Parcel parcel) {
        BigDecimal volumetric = parcel.getWidth()
                .multiply(parcel.getHeight())
                .multiply(parcel.getLength())
                .divide(BigDecimal.valueOf(VOLUMETRIC_DIVISOR), 2, RoundingMode.HALF_UP);
        return parcel.getWeight().max(volumetric);
    }
}
