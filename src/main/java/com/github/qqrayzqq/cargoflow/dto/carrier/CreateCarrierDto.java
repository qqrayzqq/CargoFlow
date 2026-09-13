package com.github.qqrayzqq.cargoflow.dto.carrier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCarrierDto(
    @NotBlank @Size(max = 50) String name,
    @NotBlank @Size(max = 100) String contactPhone
) {}
