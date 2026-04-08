package com.github.qqrayzqq.cargoflow.domain;

import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShipmentEvent {

    @EqualsAndHashCode.Include
    private Long id;

    private Long shipmentId;

    private ShipmentStatus status;

    private String location;

    private String comment;

    private OffsetDateTime createdAt;

    public ShipmentEvent(Long shipmentId, ShipmentStatus shipmentStatus, String location, String comment, OffsetDateTime createdAt) {
        this.shipmentId = shipmentId;
        this.status = shipmentStatus;
        this.location = location;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
