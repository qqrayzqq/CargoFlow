package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.dto.shipment.PublicAddress;
import com.github.qqrayzqq.cargoflow.dto.shipment.PublicShipmentEvent;
import com.github.qqrayzqq.cargoflow.dto.shipment.PublicShipmentTracking;
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
    public Shipment getShipmentById(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return shipmentService.getShipmentById(id, userDetails.getUsername());
    }

    @QueryMapping
    @PreAuthorize("permitAll()")
    public PublicShipmentTracking getShipmentByTrackingNumber(@Argument String trackingNumber) {
        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);

        List<PublicShipmentEvent> events = shipment.getEvents().stream()
                .map(event -> new PublicShipmentEvent(event.getStatus(), event.getCreatedAt()))
                .toList();

        return new PublicShipmentTracking(
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                new PublicAddress(shipment.getFromAddress().getCity()),
                new PublicAddress(shipment.getToAddress().getCity()),
                events
        );
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

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<Shipment> getShipmentsByShipperId(@Argument Long shipperId){
        return shipmentService.getShipmentsByShipperId(shipperId);
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
    public Boolean cancelShipment(@Argument Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return shipmentService.cancelShipment(id, userDetails.getUsername());
    }
}
