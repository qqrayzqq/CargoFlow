package com.github.qqrayzqq.cargoflow.dto.shipment;

import com.github.qqrayzqq.cargoflow.dto.address.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateShipmentDto(
    @NotNull @Valid AddressDto fromAddress,
    @NotNull @Valid AddressDto toAddress,
    @NotEmpty @Valid List<CreateParcelDto> parcels
) {}
