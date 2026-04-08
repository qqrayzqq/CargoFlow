package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelService {
    private final ParcelRepository parcelRepository;

    public List<Parcel> getParcelsByShipmentId(Long shipmentId){
        return parcelRepository.findByShipmentId(shipmentId);
    }
}
