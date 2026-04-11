package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.domain.ShipmentEvent;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ShipmentEventResolver {
    private final ShipmentService shipmentService;

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ShipmentEvent addShipmentEvent(@Argument Long shipmentId, @Argument ShipmentStatus status, @Argument String location, @Argument String comment){
        return shipmentService.addShipmentEvent(shipmentId, status, location, comment);
    }
}
