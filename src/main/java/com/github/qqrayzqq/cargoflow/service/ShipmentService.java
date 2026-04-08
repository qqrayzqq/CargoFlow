package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.*;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.exception.InvalidTransitionException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final GeocodingService geocodingService;
    private final ShipmentPersistenceService shipmentPersistenceService;

    public Shipment getShipmentById(Long id){
        return shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    public Shipment getShipmentByTrackingNumber(String trackingNumber){
        return shipmentRepository.findByTrackingNumber(trackingNumber).orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    public List<Shipment> getMyShipments(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        return shipmentRepository.findByShipperId(user.getId());
    }

    public List<Shipment> getAllShipments(int page, int size){
        return shipmentRepository.findAll(page, size);
    }

    public Shipment createShipment(CreateShipmentDto dto, String username){
        String fromAddressStr = addressToString(new Address(
                dto.getFromAddress().getCountry(),
                dto.getFromAddress().getZip(),
                dto.getFromAddress().getCity(),
                dto.getFromAddress().getStreet(),
                dto.getFromAddress().getBuildingNumber()
        ));

        String toAddressStr = addressToString(new Address(
                dto.getToAddress().getCountry(),
                dto.getToAddress().getZip(),
                dto.getToAddress().getCity(),
                dto.getToAddress().getStreet(),
                dto.getToAddress().getBuildingNumber()
        ));

        double[] fromCoords = geocodingService.geocode(fromAddressStr);
        double[] toCoords = geocodingService.geocode(toAddressStr);
        return shipmentPersistenceService.save(username, dto, fromCoords, toCoords);
    }

    private String addressToString(Address address) {
        return String.join(", ",
                address.getCity(),
                address.getStreet(),
                address.getBuildingNumber()
        );
    }

    public Shipment updateShipmentStatus(Long id, ShipmentStatus status){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getStatus().canTransitionTo(status)){
            throw new InvalidTransitionException("Cannot transition from " + shipment.getStatus() + " to " + status);
        }
        log.info("Shipment {} status changed to {}", id, status);
        return shipmentRepository.updateStatus(id, status);
    }

    public Shipment assignCarrier(Long id, Long carrierId) {
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        carrierRepository.findById(carrierId).orElseThrow(() -> new NotFoundException("Carrier not found"));
        log.info("Carrier {} assigned to shipment {}", carrierId, id);
        return shipmentRepository.assignCarrier(id, carrierId);
    }

    public Boolean cancelShipment(Long id){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getStatus().canTransitionTo(ShipmentStatus.CANCELLED)){
            throw new InvalidTransitionException("Cannot transition from " + shipment.getStatus() + " to CANCELLED");
        }
        shipmentRepository.updateStatus(id, ShipmentStatus.CANCELLED);
        log.info("Shipment {} status changed to CANCELLED", id);
        return true;
    }
}
