package com.github.qqrayzqq.cargoflow.dto.shipment;

import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicShipmentEvent {
    private ShipmentStatus status;
    private OffsetDateTime createdAt;
}
