package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.domain.Carrier;
import com.github.qqrayzqq.cargoflow_practice.dto.carrier.CreateCarrierDto;
import com.github.qqrayzqq.cargoflow_practice.service.CarrierService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CarrierResolver {
    private final CarrierService carrierService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Carrier getCarrierById(@Argument Long id) {
        return carrierService.getCarrierById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Carrier> getAllCarriers() {
        return carrierService.getAllCarriers();
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Carrier getCarrierByShipmentId(@Argument Long shipmentId) {
        return carrierService.getCarrierByShipmentId(shipmentId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Carrier createCarrier(@Argument CreateCarrierDto input) {
        return carrierService.createCarrier(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deactivateCarrier(@Argument Long id) {
        return carrierService.deactivateCarrier(id);
    }
}
