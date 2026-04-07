package com.github.qqrayzqq.cargoflow_practice.resolver;

import com.github.qqrayzqq.cargoflow_practice.domain.Shipment;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow_practice.graphql.ShipmentResolver;
import com.github.qqrayzqq.cargoflow_practice.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentResolverTest {

    @Mock
    ShipmentService shipmentService;

    @InjectMocks
    ShipmentResolver shipmentResolver;

    @Test
    void shouldGetShipmentByTrackingNumber(){
        Shipment expected = new Shipment();
        expected.setTrackingNumber("B7319H7421");

        when(shipmentService.getShipmentByTrackingNumber("B7319H7421")).thenReturn(expected);

        Shipment result = shipmentResolver.getShipmentByTrackingNumber("B7319H7421");

        assertEquals(expected, result);
        verify(shipmentService).getShipmentByTrackingNumber("B7319H7421");
    }

    @Test
    void shouldGetShipmentById(){
        Shipment expected = new Shipment();
        expected.setId(2L);

        when(shipmentService.getShipmentById(2L)).thenReturn(expected);

        Shipment result = shipmentResolver.getShipmentById(2L);

        assertEquals(expected, result);
        verify(shipmentService).getShipmentById(2L);
    }

    @Test
    void shouldUpdateShipmentStatus(){
        Shipment expected = new Shipment();
        expected.setId(2L);
        expected.setStatus(ShipmentStatus.AT_HUB);

        when(shipmentService.updateShipmentStatus(2L, ShipmentStatus.AT_HUB)).thenReturn(expected);

        Shipment result = shipmentResolver.updateShipmentStatus(2L, ShipmentStatus.AT_HUB);

        assertEquals(expected, result);
        verify(shipmentService).updateShipmentStatus(2L, ShipmentStatus.AT_HUB);
    }

    @Test
    void shouldCancelShipment(){
        when(shipmentService.cancelShipment(1L)).thenReturn(true);

        Boolean result = shipmentResolver.cancelShipment(1L);

        assertTrue(result);
        verify(shipmentService).cancelShipment(1L);
    }
}