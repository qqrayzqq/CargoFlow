package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Carrier;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.dto.carrier.CreateCarrierDto;
import com.github.qqrayzqq.cargoflow.exception.ForbiddenException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.CarrierRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarrierService {
    private final CarrierRepository carrierRepository;
    private final ShipmentRepository shipmentRepository;

    public Carrier getCarrierById(Long id){
        return carrierRepository.findById(id).orElseThrow(() -> new NotFoundException("Carrier not found"));
    }

    public List<Carrier> getAllCarriers(int page, int size){
        return carrierRepository.findAll(page, size);
    }

    public Carrier getCarrierByShipmentId(Long shipmentId, Long userId){
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> new NotFoundException("Shipment not found"));
        if(!shipment.getShipper().getId().equals(userId)) throw new ForbiddenException("You can't get this carrier");
        return carrierRepository.findByShipmentId(shipmentId).orElseThrow(() -> new NotFoundException("Carrier not found"));
    }

    public Carrier createCarrier(CreateCarrierDto dto){
        Carrier newCarrier = new Carrier(dto.name(), dto.contactPhone());

        log.info("Carrier {} created", newCarrier.getName());
        return carrierRepository.save(newCarrier);
    }

    public Boolean deactivateCarrier(Long id){
        if(carrierRepository.findById(id).isEmpty()){
            throw new NotFoundException("Carrier not found");
        }
        carrierRepository.deactivate(id);

        log.info("Carrier {} deactivated", carrierRepository.findById(id).get().getName());
        return true;
    }

    public Boolean activateCarrier(Long id){
        if(carrierRepository.findById(id).isEmpty()){
            throw new NotFoundException("Carrier not found");
        }
        carrierRepository.activate(id);

        log.info("Carrier {} activated", carrierRepository.findById(id).get().getName());
        return true;
    }
}
