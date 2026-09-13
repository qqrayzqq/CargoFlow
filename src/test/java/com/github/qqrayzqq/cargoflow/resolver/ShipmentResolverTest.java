package com.github.qqrayzqq.cargoflow.resolver;

import com.github.qqrayzqq.cargoflow.domain.Address;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.shipment.PublicShipmentTracking;
import com.github.qqrayzqq.cargoflow.graphql.ShipmentResolver;
import com.github.qqrayzqq.cargoflow.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentResolverTest {

    @Mock
    ShipmentService shipmentService;

    @Mock
    UserDetails userDetails;

    @InjectMocks
    ShipmentResolver shipmentResolver;

    @Test
    void shouldGetShipmentByTrackingNumber(){
        Address fromAddress = new Address("Czechia", "11000", "Prague", "Wenceslas Square", "1");
        Address toAddress = new Address("Germany", "10115", "Berlin", "Unter den Linden", "1");

        Shipment shipment = new Shipment("B7319H7421", ShipmentStatus.CREATED, OffsetDateTime.now(),
                null, null, fromAddress, toAddress);

        when(shipmentService.getShipmentByTrackingNumber("B7319H7421")).thenReturn(shipment);

        PublicShipmentTracking result = shipmentResolver.getShipmentByTrackingNumber("B7319H7421");

        assertEquals("B7319H7421", result.getTrackingNumber());
        assertEquals(ShipmentStatus.CREATED, result.getStatus());
        assertEquals("Prague", result.getFromAddress().getCity());
        assertEquals("Berlin", result.getToAddress().getCity());
        verify(shipmentService).getShipmentByTrackingNumber("B7319H7421");
    }

    @Test
    void shouldGetShipmentById(){
        Shipment expected = new Shipment();
        expected.setId(2L);

        when(userDetails.getUsername()).thenReturn("john");
        when(shipmentService.getShipmentById(2L, "john")).thenReturn(expected);

        Shipment result = shipmentResolver.getShipmentById(2L, userDetails);

        assertEquals(expected, result);
        verify(shipmentService).getShipmentById(2L, "john");
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
        when(userDetails.getUsername()).thenReturn("john");
        when(shipmentService.cancelShipment(1L, "john")).thenReturn(true);

        Boolean result = shipmentResolver.cancelShipment(1L, userDetails);

        assertTrue(result);
        verify(shipmentService).cancelShipment(1L, "john");
    }
}