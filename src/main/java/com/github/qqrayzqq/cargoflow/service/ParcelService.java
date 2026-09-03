package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.exception.ForbiddenException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.ParcelRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentRepository;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelService {
    private final ParcelRepository parcelRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;

    public List<Parcel> getParcelsByShipmentId(Long shipmentId, String username){
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(() -> new NotFoundException("Shipment not found"));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        if(!shipment.getShipper().equals(user)) throw new ForbiddenException("You don't have access to this shipment");
        return shipment.getParcels();
    }
}
