package com.github.qqrayzqq.cargoflow_practice.service;

import com.github.qqrayzqq.cargoflow_practice.domain.*;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow_practice.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow_practice.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow_practice.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow_practice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final ParcelRepository parcelRepository;
    private final AddressRepository addressRepository;
    private final ShipmentEventRepository shipmentEventRepository;

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

    public List<Shipment> getAllShipments(){
        return shipmentRepository.findAll();
    }

    public List<Shipment> searchShipments(String query){
        // todo
        return List.of();
    }

    public Shipment createShipment(CreateShipmentDto dto){
        String trackingNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new InvalidCredentialsException();
        }
        Address fromAddress = addressRepository.save(new Address(dto.getFromAddress().getCountry(), dto.getFromAddress().getZip(), dto.getFromAddress().getCity(), dto.getFromAddress().getStreet(), dto.getFromAddress().getBuildingNumber()));
        Address toAddress = addressRepository.save(new Address(dto.getToAddress().getCountry(), dto.getToAddress().getZip(), dto.getToAddress().getCity(), dto.getToAddress().getStreet(), dto.getToAddress().getBuildingNumber()));
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
        Shipment newShipment = new Shipment(trackingNumber, ShipmentStatus.CREATED, OffsetDateTime.now(), user, null, fromAddress, toAddress);
        Shipment saved = shipmentRepository.save(newShipment);
        List<Parcel> parcels = dto.getParcels().stream().map(p -> new Parcel(saved.getId(),p.getWeight(), p.getWidth(), p.getHeight(), p.getLength(), p.isFragile(), p.getDescription())).toList();
        parcelRepository.saveAll(parcels);
        saved.setParcels(parcels);
        ShipmentEvent shipmentEvent = shipmentEventRepository.save(new ShipmentEvent(saved.getId(), ShipmentStatus.CREATED, null, null, OffsetDateTime.now()));
        saved.getEvents().add(shipmentEvent);
        return shipmentRepository.save(saved);
    }

    public Shipment updateShipmentStatus(Long id, ShipmentStatus status){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        shipment.setStatus(status);
        return shipmentRepository.save(shipment);
    }

    public Shipment assignCarrier(Long id, Long carrierId) {
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        Carrier carrier = carrierRepository.findById(carrierId).orElseThrow(() -> new NotFoundException("Carrier not found"));
        shipment.setCarrier(carrier);
        return shipmentRepository.save(shipment);
    }

    public Boolean cancelShipment(Long id){
        Shipment shipment = shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(shipment);
        return true;
    }
}
