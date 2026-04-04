package com.github.qqrayzqq.cargoflow_practice.dto.shipment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateParcelDto {
    private BigDecimal weight;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal length;
    private boolean isFragile;
    private String description;
}
