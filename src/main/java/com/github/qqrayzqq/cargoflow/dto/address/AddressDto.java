package com.github.qqrayzqq.cargoflow.dto.address;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
    @NotBlank String country,
    @NotBlank String zip,
    @NotBlank String city,
    @NotBlank String street,
    @NotBlank String buildingNumber
) {}
