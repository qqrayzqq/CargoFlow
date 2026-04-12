package com.github.qqrayzqq.cargoflow.dto.shipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateParcelDto(
    @NotNull @Positive BigDecimal weight,
    @NotNull @Positive BigDecimal width,
    @NotNull @Positive BigDecimal height,
    @NotNull @Positive BigDecimal length,
    boolean isFragile,
    String description
) {}
