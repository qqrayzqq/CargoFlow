package com.github.qqrayzqq.cargoflow.graphql;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.ShipmentEvent;
import com.github.qqrayzqq.cargoflow.repository.ParcelRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @BatchMapping resolves N shipments' child collections in a single DB query, preventing N+1
@Controller
@RequiredArgsConstructor
public class ShipmentBatchResolver {

    private final ParcelRepository parcelRepository;
    private final ShipmentEventRepository shipmentEventRepository;

    @BatchMapping
    public Map<Shipment, List<Parcel>> parcels(List<Shipment> shipments) {
        List<Long> ids = shipments.stream().map(Shipment::getId).toList();
        Map<Long, List<Parcel>> grouped = parcelRepository.findAllByShipmentId(ids).stream()
                .collect(Collectors.groupingBy(Parcel::getShipmentId));
        return shipments.stream().collect(Collectors.toMap(
                s -> s,
                s -> new ArrayList<>(grouped.getOrDefault(s.getId(), List.of()))));
    }

    @BatchMapping
    public Map<Shipment, List<ShipmentEvent>> events(List<Shipment> shipments) {
        List<Long> ids = shipments.stream().map(Shipment::getId).toList();
        Map<Long, List<ShipmentEvent>> grouped = shipmentEventRepository.findAllByShipmentId(ids).stream()
                .collect(Collectors.groupingBy(ShipmentEvent::getShipmentId));
        return shipments.stream().collect(Collectors.toMap(
                s -> s,
                s -> new ArrayList<>(grouped.getOrDefault(s.getId(), List.of()))));
    }
}
