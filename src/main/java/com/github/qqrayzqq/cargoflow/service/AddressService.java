package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Address;
import com.github.qqrayzqq.cargoflow.elasticsearch.document.AddressDocument;
import com.github.qqrayzqq.cargoflow.elasticsearch.repository.AddressSearchRepository;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressSearchRepository addressSearchRepository;
    private final AddressRepository addressRepository;

    public Address getAddressById(Long id){
        return addressRepository.findById(id).orElseThrow(() -> new NotFoundException("Address not found"));
    }

    public List<Address> searchAddresses(String query){
        if(query == null || query.isBlank()) return List.of();

        List<AddressDocument> docs = addressSearchRepository.searchByQuery(query);

        return docs.stream()
                .map(doc -> new Address(
                        Long.parseLong(doc.getId()),
                        doc.getCountry(),
                        doc.getZip(),
                        doc.getCity(),
                        doc.getStreet(),
                        doc.getBuildingNumber()))
                .toList();
    }
}
