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
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    private final ShipmentEventRepository shipmentEventRepository;

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
        String fromAddressStr = new Address(
                dto.fromAddress().country(),
                dto.fromAddress().zip(),
                dto.fromAddress().city(),
                dto.fromAddress().street(),
                dto.fromAddress().buildingNumber()
        ).toDisplayString();

        String toAddressStr = new Address(
                dto.toAddress().country(),
                dto.toAddress().zip(),
                dto.toAddress().city(),
                dto.toAddress().street(),
                dto.toAddress().buildingNumber()
        ).toDisplayString();

        double[] fromCoords = geocodingService.geocode(fromAddressStr);
        double[] toCoords = geocodingService.geocode(toAddressStr);
        return shipmentPersistenceService.save(username, dto, fromCoords, toCoords);
    }

    @Transactional
    public Shipment updateShipmentStatus(Long id, ShipmentStatus status){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getStatus().canTransitionTo(status)){
            throw new InvalidTransitionException("Cannot transition from " + shipment.getStatus() + " to " + status);
        }
        shipmentEventRepository.save(new ShipmentEvent(id, status, null, null, OffsetDateTime.now()));
        log.info("Shipment {} status changed to {}", id, status);
        return shipmentRepository.updateStatus(id, status);
    }

    @Transactional
    public Shipment assignCarrier(Long id, Long carrierId) {
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        carrierRepository.findById(carrierId).orElseThrow(() -> new NotFoundException("Carrier not found"));
        log.info("Carrier {} assigned to shipment {}", carrierId, id);
        return shipmentRepository.assignCarrier(id, carrierId);
    }

    @Transactional
    public Boolean cancelShipment(Long id){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getStatus().canTransitionTo(ShipmentStatus.CANCELLED)){
            throw new InvalidTransitionException("Cannot transition from " + shipment.getStatus() + " to CANCELLED");
        }
        shipmentRepository.updateStatus(id, ShipmentStatus.CANCELLED);
        shipmentEventRepository.save(new ShipmentEvent(id, ShipmentStatus.CANCELLED, null, null, OffsetDateTime.now()));
        log.info("Shipment {} status changed to CANCELLED", id);
        return true;
    }

    @Transactional
    public ShipmentEvent addShipmentEvent(Long shipmentId, ShipmentStatus status, String location, String comment){
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getStatus().canTransitionTo(status)){
            throw new InvalidTransitionException("Cannot transition from " + shipment.getStatus() + " to " + status);
        }
        log.info("Shipment {} status changed to {}", shipmentId, status);
        shipmentRepository.updateStatus(shipmentId, status);
        return shipmentEventRepository.save(new ShipmentEvent(shipmentId, status, location, comment, OffsetDateTime.now()));
    }

    public List<Shipment> getShipmentsByShipperId(Long shipperId){
        return shipmentRepository.findByShipperId(shipperId);
    }
}
