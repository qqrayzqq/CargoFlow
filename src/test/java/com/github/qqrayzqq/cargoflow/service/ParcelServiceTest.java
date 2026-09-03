package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Parcel;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.exception.ForbiddenException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.ParcelRepository;
import com.github.qqrayzqq.cargoflow.repository.ShipmentRepository;
import com.github.qqrayzqq.cargoflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock ParcelRepository parcelRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock UserRepository userRepository;

    ParcelService parcelService;

    @BeforeEach
    void setUp() {
        parcelService = new ParcelService(parcelRepository, shipmentRepository, userRepository);
    }

    @Test
    void shouldReturnParcelsWhenOwner() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("john");

        Parcel parcel = new Parcel();
        parcel.setId(5L);

        Shipment fakeShipment = new Shipment();
        fakeShipment.setId(1L);
        fakeShipment.setShipper(owner);
        fakeShipment.setParcels(List.of(parcel));

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(owner));

        List<Parcel> result = parcelService.getParcelsByShipmentId(1L, "john");

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().getId());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenNotOwner() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("john");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("mallory");

        Shipment fakeShipment = new Shipment();
        fakeShipment.setId(1L);
        fakeShipment.setShipper(owner);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));
        when(userRepository.findByUsername("mallory")).thenReturn(Optional.of(otherUser));

        assertThrows(ForbiddenException.class, () -> parcelService.getParcelsByShipmentId(1L, "mallory"));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenShipmentNotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> parcelService.getParcelsByShipmentId(1L, "john"));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        Shipment fakeShipment = new Shipment();
        fakeShipment.setId(1L);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> parcelService.getParcelsByShipmentId(1L, "ghost"));
    }
}
