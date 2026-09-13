package com.github.qqrayzqq.cargoflow.dto.shipment;

import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicShipmentTracking {
    private String trackingNumber;
    private ShipmentStatus status;
    private PublicAddress fromAddress;
    private PublicAddress toAddress;
    private List<PublicShipmentEvent> events;
}
