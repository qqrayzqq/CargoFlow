package com.github.qqrayzqq.cargoflow.repository;
import static com.github.qqrayzqq.cargoflow.jooq.Tables.CARRIERS;
import static com.github.qqrayzqq.cargoflow.jooq.Tables.SHIPMENTS;


import com.github.qqrayzqq.cargoflow.domain.Carrier;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarrierRepository {

    private final DSLContext dsl;

    public Optional<Carrier> findById(Long id) {
        return dsl.selectFrom(CARRIERS)
                .where(CARRIERS.ID.eq(id))
                .fetchOptionalInto(Carrier.class);
    }

    public Optional<Carrier> findByShipmentId(Long shipmentId) {
        return dsl.select(CARRIERS.fields())
                .from(CARRIERS)
                .join(SHIPMENTS)
                .on(SHIPMENTS.CARRIER_ID.eq(CARRIERS.ID))
                .where(SHIPMENTS.ID.eq(shipmentId))
                .fetchOptionalInto(Carrier.class);
    }

    public List<Carrier> findAll() {
        return dsl.select(
                        CARRIERS.ID,
                        CARRIERS.NAME,
                        CARRIERS.CONTACT_PHONE,
                        CARRIERS.IS_ACTIVE
                )
                .from(CARRIERS)
                .fetchInto(Carrier.class);
    }

    public Carrier save(Carrier carrier) {
        return dsl.insertInto(CARRIERS)
                .set(CARRIERS.NAME, carrier.getName())
                .set(CARRIERS.CONTACT_PHONE, carrier.getContactPhone())
                .set(CARRIERS.IS_ACTIVE, carrier.isActive())
                .returning()
                .fetchOneInto(Carrier.class);
    }

    public boolean deactivate(Long id) {
        int updatedRows = dsl.update(CARRIERS)
                .set(CARRIERS.IS_ACTIVE, false)
                .where(CARRIERS.ID.eq(id))
                .execute();

        return updatedRows > 0;
    }

}
