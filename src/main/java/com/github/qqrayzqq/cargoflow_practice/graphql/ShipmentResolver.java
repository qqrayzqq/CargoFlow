package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.domain.Shipment;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow_practice.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow_practice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShipmentResolver {
    private final ShipmentService shipmentService;

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
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
    public List<Shipment> getMyShipments() {
        return shipmentService.getMyShipments();
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<Shipment> getAllShipments() {
        return shipmentService.getAllShipments();
    }

    @QueryMapping
    @PreAuthorize("permitAll()")
    public List<Shipment> searchShipments(@Argument String query) {
        return shipmentService.searchShipments(query);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Shipment createShipment(@Argument CreateShipmentDto input) {
        return shipmentService.createShipment(input);
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
