package com.github.qqrayzqq.cargoflow_practice.dto.shipment;

import com.github.qqrayzqq.cargoflow_practice.dto.address.AddressDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateShipmentDto {
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private List<CreateParcelDto> parcels;
}
