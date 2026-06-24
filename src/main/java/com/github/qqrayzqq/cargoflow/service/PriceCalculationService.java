package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final BigDecimal FRAGILE_MULTIPLIER = new BigDecimal("1.5");
    // Aviation industry standard: 1 kg of chargeable weight per 5000 cm³ of volume
    private static final double VOLUMETRIC_DIVISOR = 5000.0;

    @Value("${cargoflow.rate-per-kg}")
    private BigDecimal ratePerKg;

    public BigDecimal calculatePrice(Shipment shipment) {
        if (shipment.getFromAddress().getLatitude() == null || shipment.getToAddress().getLatitude() == null) {
            throw new BadRequestException("Coordinates not found for this address");
        }

        double distance = haversine(
                shipment.getFromAddress().getLatitude(), shipment.getFromAddress().getLongitude(),
                shipment.getToAddress().getLatitude(), shipment.getToAddress().getLongitude());

        BigDecimal totalWeight = BigDecimal.ZERO;
        boolean hasFragile = false;

        if (shipment.getParcels() != null) {
            for (Parcel parcel : shipment.getParcels()) {
                if (parcel.isFragile()) hasFragile = true;
                totalWeight = totalWeight.add(effectiveWeight(parcel));
            }
        }

        log.debug("distance={}, totalWeight={}", distance, totalWeight);
        BigDecimal price = totalWeight.multiply(BigDecimal.valueOf(distance)).multiply(ratePerKg);
        if (hasFragile) price = price.multiply(FRAGILE_MULTIPLIER);
        return price;
    }

    // Great-circle distance between two lat/lon points in kilometers (Haversine formula)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    // Chargeable weight = max(actual weight, volumetric weight), per shipping industry standard
    private BigDecimal effectiveWeight(Parcel parcel) {
        BigDecimal volumetric = parcel.getWidth()
                .multiply(parcel.getHeight())
                .multiply(parcel.getLength())
                .divide(BigDecimal.valueOf(VOLUMETRIC_DIVISOR), 2, RoundingMode.HALF_UP);
        return parcel.getWeight().max(volumetric);
    }
}
