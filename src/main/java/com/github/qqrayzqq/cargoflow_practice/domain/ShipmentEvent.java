package com.github.qqrayzqq.cargoflow_practice.domain;

import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
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
}
