package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.*;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.elasticsearch.dto.AddressEventDTO;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.AddressRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentEventRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentRepository;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentPersistenceService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Shipment save(String username, CreateShipmentDto dto, double[] fromCoords, double[] toCoords){
        String trackingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        while (shipmentRepository.findByTrackingNumber(trackingNumber).isPresent()){
            trackingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
        log.info("Creating shipment for user: {}", username);
        Address fromAddress = addressRepository.findOrCreate(new Address(dto.fromAddress().country(), dto.fromAddress().zip(), dto.fromAddress().city(), dto.fromAddress().street(), dto.fromAddress().buildingNumber()));
        Address toAddress = addressRepository.findOrCreate(new Address(dto.toAddress().country(), dto.toAddress().zip(), dto.toAddress().city(), dto.toAddress().street(), dto.toAddress().buildingNumber()));
        if (fromCoords != null) addressRepository.updateCoordinates(fromAddress.getId(), fromCoords[0], fromCoords[1]);
        if (toCoords != null) addressRepository.updateCoordinates(toAddress.getId(), toCoords[0], toCoords[1]);
        applicationEventPublisher.publishEvent(new AddressEventDTO(
                fromAddress.getId(),
                fromAddress.getCity(),
                fromAddress.getStreet(),
                fromAddress.getZip(),
                fromAddress.getCountry(),
                fromAddress.getBuildingNumber()
        ));
        applicationEventPublisher.publishEvent(new AddressEventDTO(
                toAddress.getId(),
                toAddress.getCity(),
                toAddress.getStreet(),
                toAddress.getZip(),
                toAddress.getCountry(),
                toAddress.getBuildingNumber()
        ));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        List<Parcel> parcels = dto.parcels().stream()
                .map(p -> new Parcel(null, p.weight(), p.width(), p.height(), p.length(), p.isFragile(), p.description()))
                .toList();
        Shipment newShipment = new Shipment(trackingNumber, ShipmentStatus.CREATED, OffsetDateTime.now(), user, null, fromAddress, toAddress);
        newShipment.setParcels(parcels);
        Shipment saved = shipmentRepository.save(newShipment);
        ShipmentEvent shipmentEvent = shipmentEventRepository.save(new ShipmentEvent(saved.getId(), ShipmentStatus.CREATED, null, null, OffsetDateTime.now()));
        saved.getEvents().add(shipmentEvent);
        log.info("Shipment created: tracking={}", saved.getTrackingNumber());
        return saved;
    }
}
