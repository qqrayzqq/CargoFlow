package com.github.qqrayzqq.cargoflow_practice.dto.carrier;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCarrierDto {
    private String name;
    private String contactPhone;
}
