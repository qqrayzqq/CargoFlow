package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Address;
import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PriceCalculationServiceTest {

    private PriceCalculationService service;

    // Прага → Брно ≈ 205 км
    private static final double PRAGUE_LAT = 50.08;
    private static final double PRAGUE_LON = 14.44;
    private static final double BRNO_LAT   = 49.19;
    private static final double BRNO_LON   = 16.61;

    @BeforeEach
    void setUp() {
        service = new PriceCalculationService();
        ReflectionTestUtils.setField(service, "ratePerKg", new BigDecimal("0.01"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Shipment shipmentWithCoords() {
        Address from = new Address("CZ", "110 00", "Prague", "Vaclavske nam", "1");
        from.setLatitude(PriceCalculationServiceTest.PRAGUE_LAT);
        from.setLongitude(PriceCalculationServiceTest.PRAGUE_LON);

        Address to = new Address("CZ", "602 00", "Brno", "Namesti Svobody", "1");
        to.setLatitude(PriceCalculationServiceTest.BRNO_LAT);
        to.setLongitude(PriceCalculationServiceTest.BRNO_LON);

        Shipment s = new Shipment();
        s.setFromAddress(from);
        s.setToAddress(to);
        return s;
    }

    private Parcel parcel(double weight, double side, boolean fragile) {
        BigDecimal w = BigDecimal.valueOf(weight);
        BigDecimal d = BigDecimal.valueOf(side);
        return new Parcel(null, w, d, d, d, fragile, "test");
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void standardPrice_isPositive() {
        Shipment s = shipmentWithCoords();
        s.setParcels(List.of(parcel(1.0, 10, false)));

        BigDecimal price = service.calculatePrice(s);

        // 1 kg * ~205 km * 0.01 ≈ 2.05
        assertThat(price).isBetween(new BigDecimal("1.5"), new BigDecimal("3.0"));
    }

    @Test
    void fragileParcel_appliesMultiplier() {
        Shipment s = shipmentWithCoords();
        s.setParcels(List.of(parcel(1.0, 10, true)));

        BigDecimal fragilePrice = service.calculatePrice(s);

        s.setParcels(List.of(parcel(1.0, 10, false)));
        BigDecimal normalPrice = service.calculatePrice(s);

        // fragile должен быть ровно в 1.5 раза дороже
        assertThat(fragilePrice).isEqualByComparingTo(
                normalPrice.multiply(new BigDecimal("1.5")).setScale(fragilePrice.scale(), java.math.RoundingMode.HALF_UP)
        );
    }

    @Test
    void volumetricWeightWins_whenBiggerThanActual() {
        // Коробка 50x50x50 см = 125_000 см³ / 5000 = 25 кг объёмного веса
        // Реальный вес = 1 кг → должен победить объёмный (25 кг)
        Shipment light = shipmentWithCoords();
        light.setParcels(List.of(new Parcel(null,
                new BigDecimal("1"),
                new BigDecimal("50"), new BigDecimal("50"), new BigDecimal("50"),
                false, "big box")));

        Shipment heavy = shipmentWithCoords();
        heavy.setParcels(List.of(parcel(25.0, 10, false)));

        // цены должны быть близки (оба используют 25 кг)
        BigDecimal lightPrice = service.calculatePrice(light);
        BigDecimal heavyPrice = service.calculatePrice(heavy);

        assertThat(lightPrice).isEqualByComparingTo(heavyPrice);
    }

    @Test
    void nullFromCoords_throwsBadRequest() {
        Shipment s = shipmentWithCoords();
        s.getFromAddress().setLatitude(null);
        s.setParcels(List.of(parcel(1.0, 10, false)));

        assertThatThrownBy(() -> service.calculatePrice(s))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void nullToCoords_throwsBadRequest() {
        Shipment s = shipmentWithCoords();
        s.getToAddress().setLatitude(null);
        s.setParcels(List.of(parcel(1.0, 10, false)));

        assertThatThrownBy(() -> service.calculatePrice(s))
                .isInstanceOf(BadRequestException.class);
    }
}