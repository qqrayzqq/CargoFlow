package com.github.qqrayzqq.cargoflow_practice.service;

import com.github.qqrayzqq.cargoflow_practice.domain.Carrier;
import com.github.qqrayzqq.cargoflow_practice.dto.carrier.CreateCarrierDto;
import com.github.qqrayzqq.cargoflow_practice.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow_practice.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierService {
    private final CarrierRepository carrierRepository;

    public Carrier getCarrierById(Long id){
        return carrierRepository.findById(id).orElseThrow(() -> new NotFoundException("Carrier not found"));
    }

    public List<Carrier> getAllCarriers(){
        return carrierRepository.findAll();
    }

    public Carrier getCarrierByShipmentId(Long shipmentId){
        return carrierRepository.findByShipmentId(shipmentId).orElseThrow(() -> new NotFoundException("Carrier not found"));
    }

    public Carrier createCarrier(CreateCarrierDto dto){
        Carrier newCarrier = new Carrier(dto.getName(), dto.getContactPhone());
        return carrierRepository.save(newCarrier);
    }

    public Boolean deactivateCarrier(Long id){
        if(carrierRepository.findById(id).isEmpty()){
            throw new NotFoundException("Carrier not found");
        }
        carrierRepository.deactivate(id);
        return true;
    }
}
