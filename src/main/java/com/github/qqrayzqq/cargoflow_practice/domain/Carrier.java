package com.github.qqrayzqq.cargoflow_practice.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Carrier {

    @EqualsAndHashCode.Include
    private Long id;

    private String name;

    private String contactPhone;

    private boolean isActive;
}
