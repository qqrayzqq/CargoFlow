package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.service.ParcelService;
import com.github.qqrayzqq.cargoflow.service.PriceCalculationService;
import com.github.qqrayzqq.cargoflow.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class PriceResolver {

    private final PriceCalculationService priceCalculationService;
    private final ShipmentService shipmentService;
    private final ParcelService parcelService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public BigDecimal shipmentPrice(@Argument Long id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        shipment.setParcels(parcelService.getParcelsByShipmentId(id));
        return priceCalculationService.calculatePrice(shipment);
    }
}
