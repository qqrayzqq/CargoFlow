package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Address;
import com.github.qqrayzqq.cargoflow.domain.Carrier;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.address.AddressDto;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateParcelDto;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.exception.InvalidCredentialsException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock UserRepository userRepository;
    @Mock CarrierRepository carrierRepository;
    @Mock AddressRepository addressRepository;
    @Mock ShipmentEventRepository shipmentEventRepository;
    @Mock ApplicationEventPublisher applicationEventPublisher;
    @Mock GeocodingService geocodingService;

    ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(
                shipmentRepository, userRepository, carrierRepository,
                addressRepository, shipmentEventRepository, applicationEventPublisher, geocodingService
        );
    }

    @Nested
    class testGetShipmentById{
        @Test
        void shouldReturnShipmentWhenFound() {
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setTrackingNumber("18321EB819");

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            Shipment result = shipmentService.getShipmentById(1L);

            assertEquals("18321EB819", result.getTrackingNumber());
        }

        @Test
        void shouldThrowNotFoundException() {
            assertThrows(NotFoundException.class, () -> shipmentService.getShipmentById(1L));
        }
    }

    @Nested
    class testGetShipmentByTrackingNumber{
        @Test
        void shouldReturnShipmentWhenFound(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setTrackingNumber("2H2932W8283N");

            when(shipmentRepository.findByTrackingNumber("2H2932W8283N")).thenReturn(Optional.of(fakeShipment));

            Shipment result = shipmentService.getShipmentByTrackingNumber("2H2932W8283N");

            assertEquals(1L, result.getId());
        }

        @Test
        void shouldThrowNotFoundException(){
            assertThrows(NotFoundException.class, () -> shipmentService.getShipmentByTrackingNumber("7218H47281M"));
        }
    }

    @Nested
    class testCreateShipment{
        @AfterEach
        void clearSecurityContext() {
            SecurityContextHolder.clearContext();
        }


        private AddressDto makeAddress() {
            AddressDto a = new AddressDto();
            a.setCountry("Czech Republic");
            a.setZip("11000");
            a.setCity("Prague");
            a.setStreet("Václavské náměstí");
            a.setBuildingNumber("1");
            return a;
        }

        @Test
        void shouldReturnShipmentWhenCreated(){
            CreateParcelDto parcel = new CreateParcelDto();
            parcel.setWeight(new BigDecimal("2.5"));
            parcel.setWidth(new BigDecimal("30"));
            parcel.setHeight(new BigDecimal("20"));
            parcel.setLength(new BigDecimal("40"));
            parcel.setFragile(false);
            parcel.setDescription("Books");

            CreateShipmentDto dto = new CreateShipmentDto();
            dto.setFromAddress(makeAddress());
            dto.setToAddress(makeAddress());
            dto.setParcels(List.of(parcel));

            var auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Address fakeAddress = new Address();
            fakeAddress.setId(1L);
            when(addressRepository.save(any())).thenReturn(fakeAddress);

            User fakeUser = new User();
            fakeUser.setId(1L);
            fakeUser.setUsername("testuser");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(fakeUser));

            Shipment fakeShipment = new Shipment();
            fakeShipment.setTrackingNumber("ABC123");
            fakeShipment.setEvents(new ArrayList<>());
            when(shipmentRepository.save(any())).thenReturn(fakeShipment);

            Shipment result = shipmentService.createShipment(dto);
            assertEquals("ABC123", result.getTrackingNumber());
        }

        @Test
        void shouldThrowInvalidCredentialsWhenNotAuthenticated() {
            CreateShipmentDto dto = new CreateShipmentDto();
            dto.setFromAddress(makeAddress());
            dto.setToAddress(makeAddress());
            dto.setParcels(List.of());

            assertThrows(InvalidCredentialsException.class, () -> shipmentService.createShipment(dto));
        }

        @Test
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            var auth = new UsernamePasswordAuthenticationToken("ghost", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            Address fakeAddress = new Address();
            fakeAddress.setId(2L);
            when(addressRepository.save(any())).thenReturn(fakeAddress);

            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            CreateShipmentDto dto = new CreateShipmentDto();
            dto.setFromAddress(makeAddress());
            dto.setToAddress(makeAddress());
            dto.setParcels(List.of());

            assertThrows(NotFoundException.class, () -> shipmentService.createShipment(dto));
        }
    }

    @Nested
    class testGetAllShipments{
        @Test
        void shouldCallRepositoryWithCorrectPageAndSize(){
            shipmentService.getAllShipments(1, 10);

            verify(shipmentRepository).findAll(1,10);
        }
    }

    @Nested
    class testCancelShipment{
        @Test
        void shouldCancelShipmentCorrect(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.CREATED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            assertTrue(shipmentService.cancelShipment(1L));

            verify(shipmentRepository).updateStatus(1L, ShipmentStatus.CANCELLED);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenShipmentNotFound(){
            assertThrows(NotFoundException.class, () -> shipmentService.cancelShipment(1L));
        }
    }

    @Nested
    class testUpdateShipmentStatus{
        @Test
        void shouldUpdateShipmentStatusCorrect(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.CREATED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            shipmentService.updateShipmentStatus(1L, ShipmentStatus.IN_TRANSIT);

            verify(shipmentRepository).updateStatus(1L, ShipmentStatus.IN_TRANSIT);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenShipmentNotFound(){
            assertThrows(NotFoundException.class, () -> shipmentService.updateShipmentStatus(1L, ShipmentStatus.IN_TRANSIT));
        }
    }

    @Nested
    class testAssignCarrier{
        @Test
        void shouldAssignCarrierCorrect(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.CREATED);

            Carrier fakeCarrier = new Carrier();
            fakeCarrier.setId(2L);
            fakeCarrier.setName("DHL");

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));
            when(carrierRepository.findById(2L)).thenReturn(Optional.of(fakeCarrier));

            shipmentService.assignCarrier(1L, 2L);

            verify(shipmentRepository).assignCarrier(1L, 2L);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenShipmentNotFound(){
            assertThrows(NotFoundException.class, () -> shipmentService.assignCarrier(1L, 2L));
        }

        @Test
        void shouldThrowNotFoundExceptionWhenCarrierNotFound(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.CREATED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            assertThrows(NotFoundException.class, () -> shipmentService.assignCarrier(1L, 2L));
        }
    }
}
