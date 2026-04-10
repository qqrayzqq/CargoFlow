package com.github.qqrayzqq.cargoflow.repository;

import com.github.qqrayzqq.cargoflow.domain.*;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.qqrayzqq.cargoflow.jooq.Tables.*;

@Repository
@RequiredArgsConstructor
public class ShipmentRepository {

    private final DSLContext dsl;
    private final ParcelRepository parcelRepository;
    private final ShipmentEventRepository shipmentEventRepository;

    private Shipment mapRecord(org.jooq.Record record) {
        var FROM_ADDR = ADDRESSES.as("from_addr");
        var TO_ADDR = ADDRESSES.as("to_addr");

        Carrier carrier = null;
        if (record.get(CARRIERS.ID) != null) {
            carrier = new Carrier();
            carrier.setId(record.get(CARRIERS.ID));
            carrier.setName(record.get(CARRIERS.NAME));
            carrier.setContactPhone(record.get(CARRIERS.CONTACT_PHONE));
            carrier.setActive(record.get(CARRIERS.IS_ACTIVE));
        }

        User shipper = new User();
        shipper.setId(record.get(USERS.ID));
        shipper.setUsername(record.get(USERS.USERNAME));
        shipper.setEmail(record.get(USERS.EMAIL));
        shipper.setFullName(record.get(USERS.FULL_NAME));
        shipper.setRole(UserRole.valueOf(record.get(USERS.ROLE)));

        Address fromAddress = new Address();
        fromAddress.setId(record.get(FROM_ADDR.ID));
        fromAddress.setCountry(record.get(FROM_ADDR.COUNTRY));
        fromAddress.setCity(record.get(FROM_ADDR.CITY));
        fromAddress.setZip(record.get(FROM_ADDR.ZIP));
        fromAddress.setStreet(record.get(FROM_ADDR.STREET));
        fromAddress.setBuildingNumber(record.get(FROM_ADDR.BUILDING_NUMBER));
        fromAddress.setLatitude(record.get(FROM_ADDR.LATITUDE));
        fromAddress.setLongitude(record.get(FROM_ADDR.LONGITUDE));

        Address toAddress = new Address();
        toAddress.setId(record.get(TO_ADDR.ID));
        toAddress.setCountry(record.get(TO_ADDR.COUNTRY));
        toAddress.setCity(record.get(TO_ADDR.CITY));
        toAddress.setZip(record.get(TO_ADDR.ZIP));
        toAddress.setStreet(record.get(TO_ADDR.STREET));
        toAddress.setBuildingNumber(record.get(TO_ADDR.BUILDING_NUMBER));
        toAddress.setLatitude(record.get(TO_ADDR.LATITUDE));
        toAddress.setLongitude(record.get(TO_ADDR.LONGITUDE));

        Shipment shipment = new Shipment();
        shipment.setId(record.get(SHIPMENTS.ID));
        shipment.setTrackingNumber(record.get(SHIPMENTS.TRACKING_NUMBER));
        shipment.setStatus(ShipmentStatus.valueOf(record.get(SHIPMENTS.STATUS)));
        shipment.setCreatedAt(record.get(SHIPMENTS.CREATED_AT));
        shipment.setCarrier(carrier);
        shipment.setShipper(shipper);
        shipment.setFromAddress(fromAddress);
        shipment.setToAddress(toAddress);

        return shipment;
    }

    private org.jooq.SelectOnConditionStep<?> baseSelect() {
        var FROM_ADDR = ADDRESSES.as("from_addr");
        var TO_ADDR = ADDRESSES.as("to_addr");
        return dsl.select()
                .from(SHIPMENTS)
                .leftJoin(CARRIERS).on(SHIPMENTS.CARRIER_ID.eq(CARRIERS.ID))
                .join(USERS).on(SHIPMENTS.SHIPPER_ID.eq(USERS.ID))
                .join(FROM_ADDR).on(SHIPMENTS.FROM_ADDRESS_ID.eq(FROM_ADDR.ID))
                .join(TO_ADDR).on(SHIPMENTS.TO_ADDRESS_ID.eq(TO_ADDR.ID));
    }

    private void enrichWithParcels(List<Shipment> shipments){
        List<Long> shipmentsId = shipments.stream().map(Shipment::getId).toList();
        List<Parcel> allParcels = parcelRepository.findAllByShipmentId(shipmentsId);

        Map<Long, List<Parcel>> byShipmentId = allParcels.stream()
                .collect(Collectors.groupingBy(Parcel::getShipmentId));

        for(Shipment s : shipments){
            s.setParcels(new ArrayList<>(byShipmentId.getOrDefault(s.getId(), List.of())));
        }
    }

    private void enrichWithEvents(List<Shipment> shipments){
        List<Long> shipmentsId = shipments.stream().map(Shipment::getId).toList();
        List<ShipmentEvent> allEvents = shipmentEventRepository.findAllByShipmentId(shipmentsId);

        Map<Long, List<ShipmentEvent>> byShipmentId = allEvents.stream()
                .collect(Collectors.groupingBy(ShipmentEvent::getShipmentId));

        for(Shipment s : shipments){
            s.setEvents(new ArrayList<>(byShipmentId.getOrDefault(s.getId(), List.of())));
        }
    }

    public Optional<Shipment> findById(Long id) {
        Optional<Shipment> shipment =  Optional.ofNullable(
                baseSelect()
                        .where(SHIPMENTS.ID.eq(id))
                        .fetchOne(this::mapRecord)
        );

        shipment.ifPresent(s -> {
            enrichWithParcels(List.of(s));
            enrichWithEvents(List.of(s));
        });

        return shipment;
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        Optional<Shipment> shipment = Optional.ofNullable(
                baseSelect()
                        .where(SHIPMENTS.TRACKING_NUMBER.eq(trackingNumber))
                        .fetchOne(this::mapRecord)
        );

        shipment.ifPresent(s -> {
            enrichWithParcels(List.of(s));
            enrichWithEvents(List.of(s));
        });

        return shipment;
    }

    public List<Shipment> findByShipperId(Long shipperId) {
        List<Shipment> shipments = baseSelect()
                .where(SHIPMENTS.SHIPPER_ID.eq(shipperId))
                .fetch(this::mapRecord);

        enrichWithParcels(shipments);
        enrichWithEvents(shipments);
        return shipments;
    }

    public List<Shipment> findAll(int page, int size) {
        List<Shipment> shipments =  baseSelect()
                .limit(size)
                .offset((long) page * size)
                .fetch(this::mapRecord);

        enrichWithParcels(shipments);
        enrichWithEvents(shipments);
        return shipments;
    }

    public Shipment save(Shipment shipment) {
        // Адреса уже сохранены сервисом — берём их id напрямую
        Long shipmentId = dsl.insertInto(SHIPMENTS)
                .set(SHIPMENTS.TRACKING_NUMBER, shipment.getTrackingNumber())
                .set(SHIPMENTS.STATUS, shipment.getStatus().name())
                .set(SHIPMENTS.SHIPPER_ID, shipment.getShipper().getId())
                .set(SHIPMENTS.CARRIER_ID,
                        shipment.getCarrier() != null ? shipment.getCarrier().getId() : null)
                .set(SHIPMENTS.FROM_ADDRESS_ID, shipment.getFromAddress().getId())
                .set(SHIPMENTS.TO_ADDRESS_ID, shipment.getToAddress().getId())
                .returning(SHIPMENTS.ID)
                .fetchOne(SHIPMENTS.ID);

        if (shipment.getParcels() != null && !shipment.getParcels().isEmpty()) {
            shipment.getParcels().forEach(p -> p.setShipmentId(shipmentId));
            parcelRepository.saveAll(shipment.getParcels());
        }

        return findById(shipmentId).orElseThrow();
    }

    public Shipment updateStatus(Long id, ShipmentStatus status) {
        dsl.update(SHIPMENTS)
                .set(SHIPMENTS.STATUS, status.name())
                .where(SHIPMENTS.ID.eq(id))
                .execute();

        return findById(id).orElseThrow();
    }


    public Shipment assignCarrier(Long shipmentId, Long carrierId) {
        dsl.update(SHIPMENTS)
                .set(SHIPMENTS.CARRIER_ID, carrierId)
                .where(SHIPMENTS.ID.eq(shipmentId))
                .execute();

        return findById(shipmentId).orElseThrow();
    }
}
