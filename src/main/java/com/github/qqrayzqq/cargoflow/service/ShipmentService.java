package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.*;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.elasticsearch.dto.AddressEventDTO;
import com.github.qqrayzqq.cargoflow.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final AddressRepository addressRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GeocodingService geocodingService;

    public Shipment getShipmentById(Long id){
        return shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    public Shipment getShipmentByTrackingNumber(String trackingNumber){
        return shipmentRepository.findByTrackingNumber(trackingNumber).orElseThrow(() -> new NotFoundException("Shipment not found"));
    }

    public List<Shipment> getMyShipments(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new InvalidCredentialsException();
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        return shipmentRepository.findByShipperId(user.getId());
    }

    public List<Shipment> getAllShipments(int page, int size){
        return shipmentRepository.findAll(page, size);
    }

    @Transactional
    public Shipment createShipment(CreateShipmentDto dto){
        String trackingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new InvalidCredentialsException();
        }
        log.info("Creating shipment for user: {}", auth.getName());
        Address fromAddress = addressRepository.save(new Address(dto.getFromAddress().getCountry(), dto.getFromAddress().getZip(), dto.getFromAddress().getCity(), dto.getFromAddress().getStreet(), dto.getFromAddress().getBuildingNumber()));
        addressToStringAndUpdateCoordinates(fromAddress);
        Address toAddress = addressRepository.save(new Address(dto.getToAddress().getCountry(), dto.getToAddress().getZip(), dto.getToAddress().getCity(), dto.getToAddress().getStreet(), dto.getToAddress().getBuildingNumber()));
        addressToStringAndUpdateCoordinates(toAddress);
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
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

    private void addressToStringAndUpdateCoordinates(Address address) {
        String addressStr = String.join(", ",
                address.getCity(),
                address.getStreet(),
                address.getBuildingNumber()
        );
        double[] toCoords = geocodingService.geocode(addressStr);
        if(toCoords != null) addressRepository.updateCoordinates(address.getId(), toCoords[0], toCoords[1]);
        try { Thread.sleep(1100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        applicationEventPublisher.publishEvent(new AddressEventDTO(
                address.getId(), address.getCity(), address.getStreet(),
                address.getZip(), address.getCountry(), address.getBuildingNumber()
        ));
    }

    public Shipment updateShipmentStatus(Long id, ShipmentStatus status){
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
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
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        shipmentRepository.updateStatus(id, ShipmentStatus.CANCELLED);
        log.info("Shipment {} status changed to CANCELLED", id);
        return true;
    }
}
