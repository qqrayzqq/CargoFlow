package com.github.qqrayzqq.cargoflow_practice.dto.shipment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateParcelDto {
    @NotNull
    @Positive
    private BigDecimal weight;
    @NotNull
    @Positive
    private BigDecimal width;
    @NotNull
    @Positive
    private BigDecimal height;
    @NotNull
    @Positive
    private BigDecimal length;
    private boolean isFragile;
    private String description;
}
