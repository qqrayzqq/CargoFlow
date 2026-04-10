package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShipmentResolver {
    private final ShipmentService shipmentService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Shipment getShipmentById(@Argument Long id) {
        return shipmentService.getShipmentById(id);
    }

    @QueryMapping
    @PreAuthorize("permitAll()")
    public Shipment getShipmentByTrackingNumber(@Argument String trackingNumber) {
        return shipmentService.getShipmentByTrackingNumber(trackingNumber);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Shipment> getMyShipments(@AuthenticationPrincipal UserDetails userDetails) {
        return shipmentService.getMyShipments(userDetails.getUsername());
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<Shipment> getAllShipments(@Argument Integer page, @Argument Integer size) {
        return shipmentService.getAllShipments(page != null ? page : 0, size != null ? size : 20);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Shipment createShipment(@Argument @Valid CreateShipmentDto input, @AuthenticationPrincipal UserDetails userDetails) {
        return shipmentService.createShipment(input, userDetails.getUsername());
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Shipment updateShipmentStatus(@Argument Long id, @Argument ShipmentStatus status) {
        return shipmentService.updateShipmentStatus(id, status);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Shipment assignCarrier(@Argument Long id, @Argument Long carrierId) {
        return shipmentService.assignCarrier(id, carrierId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean cancelShipment(@Argument Long id) {
        return shipmentService.cancelShipment(id);
    }
}
