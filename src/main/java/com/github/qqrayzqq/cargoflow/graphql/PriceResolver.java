package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.service.PriceCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class PriceResolver {

    private final PriceCalculationService priceCalculationService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public BigDecimal shipmentPrice(@Argument Long id) {
        return priceCalculationService.calculatePrice(id);
    }
}
