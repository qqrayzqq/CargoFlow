package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.domain.Parcel;
import com.github.qqrayzqq.cargoflow_practice.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ParcelResolver {
    private final ParcelService parcelService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Parcel> getParcelsByShipmentId(@Argument Long shipmentId) {
        return parcelService.getParcelsByShipmentId(shipmentId);
    }
}
