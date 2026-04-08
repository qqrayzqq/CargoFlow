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
        log.info("Creating shipment for user: {}", username);
        Address fromAddress = addressRepository.save(new Address(dto.getFromAddress().getCountry(), dto.getFromAddress().getZip(), dto.getFromAddress().getCity(), dto.getFromAddress().getStreet(), dto.getFromAddress().getBuildingNumber()));
        Address toAddress = addressRepository.save(new Address(dto.getToAddress().getCountry(), dto.getToAddress().getZip(), dto.getToAddress().getCity(), dto.getToAddress().getStreet(), dto.getToAddress().getBuildingNumber()));
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
        List<Parcel> parcels = dto.getParcels().stream()
                .map(p -> new Parcel(null, p.getWeight(), p.getWidth(), p.getHeight(), p.getLength(), p.isFragile(), p.getDescription()))
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
