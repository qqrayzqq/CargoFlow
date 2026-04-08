package com.github.qqrayzqq.cargoflow.dto.shipment;

import com.github.qqrayzqq.cargoflow.dto.address.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateShipmentDto {
    @NotNull
    @Valid
    private AddressDto fromAddress;
    @NotNull
    @Valid
    private AddressDto toAddress;
    @NotEmpty
    @Valid
    private List<CreateParcelDto> parcels;
}
