package com.github.qqrayzqq.cargoflow_practice.repository;
import static com.github.qqrayzqq.cargoflow_practice.jooq.Tables.SHIPMENT_EVENTS;

import com.github.qqrayzqq.cargoflow_practice.domain.ShipmentEvent;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShipmentEventRepository {

    private final DSLContext dsl;

    public List<ShipmentEvent> findByShipmentId(Long shipmentId) {
        return dsl.selectFrom(SHIPMENT_EVENTS)
                .where(SHIPMENT_EVENTS.SHIPMENT_ID.eq(shipmentId))
                .fetchInto(ShipmentEvent.class);
    }

    public ShipmentEvent save(ShipmentEvent event) {
        return dsl.insertInto(SHIPMENT_EVENTS)
                .set(SHIPMENT_EVENTS.SHIPMENT_ID, event.getShipmentId())
                .set(SHIPMENT_EVENTS.COMMENT, event.getComment())
                .set(SHIPMENT_EVENTS.CREATED_AT, event.getCreatedAt())
                .set(SHIPMENT_EVENTS.LOCATION, event.getLocation())
                .set(SHIPMENT_EVENTS.STATUS, event.getStatus().name())
                .returning()
                .fetchOneInto(ShipmentEvent.class);
    }
}
