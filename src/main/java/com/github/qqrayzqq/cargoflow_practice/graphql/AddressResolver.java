package com.github.qqrayzqq.cargoflow_practice.graphql;

import com.github.qqrayzqq.cargoflow_practice.domain.Address;
import com.github.qqrayzqq.cargoflow_practice.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AddressResolver {
    private final AddressService addressService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Address getAddressById(@Argument Long id) {
        return addressService.getAddressById(id);
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<Address> searchAddresses(@Argument String query) {
        return addressService.searchAddresses(query);
    }
}
