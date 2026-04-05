package com.github.qqrayzqq.cargoflow_practice.domain;

import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Shipment {

    @EqualsAndHashCode.Include
    private Long id;

    private String trackingNumber;

    private ShipmentStatus status;

    private OffsetDateTime createdAt;

    private User shipper;

    private Carrier carrier;

    private Address fromAddress;

    private Address toAddress;

    private List<Parcel> parcels = new ArrayList<>();

    private List<ShipmentEvent> events = new ArrayList<>();

    public Shipment(String trackingNumber, ShipmentStatus status, OffsetDateTime createdAt, User shipper, Carrier carrier, Address fromAddress, Address toAddress) {
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.shipper = shipper;
        this.carrier = carrier;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
    }
}
