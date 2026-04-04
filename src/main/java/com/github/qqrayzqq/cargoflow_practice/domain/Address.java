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

    private String address;

    private String buildingNumber;
}
