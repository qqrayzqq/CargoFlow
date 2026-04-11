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

@Controller
@RequiredArgsConstructor
public class ShipmentBatchResolver {
    private final ParcelRepository parcelRepository;
    private final ShipmentEventRepository shipmentEventRepository;

    @BatchMapping
    public Map<Shipment, List<Parcel>> parcels(List<Shipment> shipments){
        List<Long> ids = shipments.stream().map(Shipment::getId).toList();
        List<Parcel> parcels = parcelRepository.findAllByShipmentId(ids);
        Map<Long, List<Parcel>> grouped = parcels.stream().collect(Collectors.groupingBy(Parcel::getShipmentId));
        return shipments.stream().collect(Collectors.toMap(shipment -> shipment, shipment -> new ArrayList<>(grouped.getOrDefault(shipment.getId(), List.of()))));
    }

    @BatchMapping
    public Map<Shipment, List<ShipmentEvent>> events(List<Shipment> shipments){
        List<Long> ids = shipments.stream().map(Shipment::getId).toList();
        List<ShipmentEvent> events = shipmentEventRepository.findAllByShipmentId(ids);
        Map<Long, List<ShipmentEvent>> grouped = events.stream().collect(Collectors.groupingBy(ShipmentEvent::getShipmentId));
        return shipments.stream().collect(Collectors.toMap(shipment -> shipment, shipment -> new ArrayList<>(grouped.getOrDefault(shipment.getId(), List.of()))));
    }
}
