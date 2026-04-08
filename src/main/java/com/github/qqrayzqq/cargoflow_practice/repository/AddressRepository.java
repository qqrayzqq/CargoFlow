package com.github.qqrayzqq.cargoflow_practice.repository;

import com.github.qqrayzqq.cargoflow_practice.domain.Address;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.github.qqrayzqq.cargoflow_practice.jooq.Tables.ADDRESSES;

@Repository
@RequiredArgsConstructor
public class AddressRepository {

    private final DSLContext dsl;

    public Optional<Address> findById(Long id) {
        return dsl.selectFrom(ADDRESSES)
                .where(ADDRESSES.ID.eq(id))
                .fetchOptionalInto(Address.class);
    }

    public void updateCoordinates(Long id, Double lat, Double lon){
        dsl.update(ADDRESSES)
                .set(ADDRESSES.LATITUDE, lat)
                .set(ADDRESSES.LONGITUDE, lon)
                .where(ADDRESSES.ID.eq(id))
                .execute();
    }

    public Address save(Address address) {
        return dsl.insertInto(ADDRESSES)
                .set(ADDRESSES.CITY, address.getCity())
                .set(ADDRESSES.BUILDING_NUMBER, address.getBuildingNumber())
                .set(ADDRESSES.COUNTRY, address.getCountry())
                .set(ADDRESSES.ZIP, address.getZip())
                .set(ADDRESSES.STREET, address.getStreet())
                .returning()
                .fetchOneInto(Address.class);
    }
}
