package com.github.qqrayzqq.cargoflow_practice.dto.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressDto {
    private String country;
    private String zip;
    private String city;
    private String address;
    private String buildingNumber;
}
