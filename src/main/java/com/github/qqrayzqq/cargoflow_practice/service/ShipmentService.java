package com.github.qqrayzqq.cargoflow_practice.service;

import com.github.qqrayzqq.cargoflow_practice.domain.*;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow_practice.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow_practice.elasticsearch.dto.AddressEventDTO;
import com.github.qqrayzqq.cargoflow_practice.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow_practice.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow_practice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final AddressRepository addressRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
        Address fromAddress = addressRepository.save(new Address(dto.getFromAddress().getCountry(), dto.getFromAddress().getZip(), dto.getFromAddress().getCity(), dto.getFromAddress().getStreet(), dto.getFromAddress().getBuildingNumber()));
        applicationEventPublisher.publishEvent(new AddressEventDTO(
                fromAddress.getId(), fromAddress.getCity(), fromAddress.getStreet(),
                fromAddress.getZip(), fromAddress.getCountry(), fromAddress.getBuildingNumber()
        ));
        Address toAddress = addressRepository.save(new Address(dto.getToAddress().getCountry(), dto.getToAddress().getZip(), dto.getToAddress().getCity(), dto.getToAddress().getStreet(), dto.getToAddress().getBuildingNumber()));
        applicationEventPublisher.publishEvent(new AddressEventDTO(
                toAddress.getId(), toAddress.getCity(), toAddress.getStreet(),
                toAddress.getZip(), toAddress.getCountry(), toAddress.getBuildingNumber()
        ));
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
        List<Parcel> parcels = dto.getParcels().stream()
                .map(p -> new Parcel(null, p.getWeight(), p.getWidth(), p.getHeight(), p.getLength(), p.isFragile(), p.getDescription()))
                .toList();
        Shipment newShipment = new Shipment(trackingNumber, ShipmentStatus.CREATED, OffsetDateTime.now(), user, null, fromAddress, toAddress);
        newShipment.setParcels(parcels);
        Shipment saved = shipmentRepository.save(newShipment);
        ShipmentEvent shipmentEvent = shipmentEventRepository.save(new ShipmentEvent(saved.getId(), ShipmentStatus.CREATED, null, null, OffsetDateTime.now()));
        saved.getEvents().add(shipmentEvent);
        return saved;
    }

    public Shipment updateShipmentStatus(Long id, ShipmentStatus status){
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        return shipmentRepository.updateStatus(id, status);
    }

    public Shipment assignCarrier(Long id, Long carrierId) {
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        carrierRepository.findById(carrierId).orElseThrow(() -> new NotFoundException("Carrier not found"));
        return shipmentRepository.assignCarrier(id, carrierId);
    }

    public Boolean cancelShipment(Long id){
        shipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Shipment not found"));
        shipmentRepository.updateStatus(id, ShipmentStatus.CANCELLED);
        return true;
    }
}
