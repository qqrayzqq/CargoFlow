package com.github.qqrayzqq.cargoflow_practice.repository;

import com.github.qqrayzqq.cargoflow_practice.domain.Parcel;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.github.qqrayzqq.cargoflow_practice.jooq.Tables.PARCELS;

@Repository
@RequiredArgsConstructor
public class ParcelRepository {

    private final DSLContext dsl;

    public List<Parcel> findByShipmentId(Long shipmentId) {
        return dsl.selectFrom(PARCELS)
                .where(PARCELS.SHIPMENT_ID.eq(shipmentId))
                .fetchInto(Parcel.class);
    }

    public List<Parcel> saveAll(List<Parcel> parcels) {
        if(parcels.isEmpty()) return List.of();

        var insert = dsl.insertInto(PARCELS,
                PARCELS.SHIPMENT_ID,
                PARCELS.WEIGHT,
                PARCELS.WIDTH,
                PARCELS.HEIGHT,
                PARCELS.LENGTH,
                PARCELS.IS_FRAGILE,
                PARCELS.DESCRIPTION
        );

        for(Parcel parcel : parcels){
            insert = insert.values(
                    parcel.getShipmentId(),
                    parcel.getWeight(),
                    parcel.getWidth(),
                    parcel.getHeight(),
                    parcel.getLength(),
                    parcel.isFragile(),
                    parcel.getDescription()
            );
        }

        return insert.returning().fetchInto(Parcel.class);
    }
}
