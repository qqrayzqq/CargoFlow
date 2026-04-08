package com.github.qqrayzqq.cargoflow.domain;

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

    public Parcel(Long shipmentId, BigDecimal weight, BigDecimal width, BigDecimal height, BigDecimal length, boolean fragile, String description) {
        this.shipmentId = shipmentId;
        this.weight = weight;
        this.width = width;
        this.height = height;
        this.length = length;
        this.isFragile = fragile;
        this.description = description;
    }
}
