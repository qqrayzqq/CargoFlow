package com.github.qqrayzqq.cargoflow_practice.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    @EqualsAndHashCode.Include
    private Long id;

    private String country;

    private String zip;

    private String city;

    private String street;

    private String buildingNumber;

    private Double latitude;

    private Double longitude;

    public Address(String country, String zip, String city, String street, String buildingNumber) {
        this.country = country;
        this.zip = zip;
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
    }

    public Address(Long id, String country, String zip, String city, String street, String buildingNumber) {
        this.id = id;
        this.country = country;
        this.zip = zip;
        this.city = city;
        this.street = street;
         this.buildingNumber = buildingNumber;
    }
}
