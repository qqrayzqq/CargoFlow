package com.github.qqrayzqq.cargoflow_practice.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Parcel {

    @EqualsAndHashCode.Include
    private Long id;

    private Long shipmentId;

    private BigDecimal weight;

    private BigDecimal width;

    private BigDecimal height;

    private BigDecimal length;

    private boolean isFragile;

    private String description;
}
