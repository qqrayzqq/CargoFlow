package com.github.qqrayzqq.cargoflow_practice.elasticsearch.dto;

public record AddressEventDTO(
    Long id,
    String city,
    String street,
    String zip,
    String country,
    String buildingNumber
) {
}
