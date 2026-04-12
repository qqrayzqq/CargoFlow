package com.github.qqrayzqq.cargoflow.service;

import com.github.qqrayzqq.cargoflow.domain.Carrier;
import com.github.qqrayzqq.cargoflow.domain.Shipment;
import com.github.qqrayzqq.cargoflow.domain.User;
import com.github.qqrayzqq.cargoflow.domain.enums.ShipmentStatus;
import com.github.qqrayzqq.cargoflow.dto.address.AddressDto;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateParcelDto;
import com.github.qqrayzqq.cargoflow.dto.shipment.CreateShipmentDto;
import com.github.qqrayzqq.cargoflow.exception.InvalidTransitionException;
import com.github.qqrayzqq.cargoflow.exception.NotFoundException;
import com.github.qqrayzqq.cargoflow.repository.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock UserRepository userRepository;
    @Mock CarrierRepository carrierRepository;
    @Mock GeocodingService geocodingService;
    @Mock ShipmentPersistenceService shipmentPersistenceService;
    @Mock ShipmentEventRepository shipmentEventRepository;

    ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(
                shipmentRepository, userRepository, carrierRepository,
                geocodingService, shipmentPersistenceService, shipmentEventRepository
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

        private AddressDto makeAddress() {
            return new AddressDto("Czech Republic", "11000", "Prague", "Václavské náměstí", "1");
        }

        @Test
        void shouldReturnShipmentWhenCreated(){
            CreateParcelDto parcel = new CreateParcelDto(
                    new BigDecimal("2.5"), new BigDecimal("30"), new BigDecimal("20"), new BigDecimal("40"), false, "Books"
            );

            CreateShipmentDto dto = new CreateShipmentDto(makeAddress(), makeAddress(), List.of(parcel));

            double[] fakeCoords = {50.0, 14.0};
            when(geocodingService.geocode(anyString())).thenReturn(fakeCoords);

            Shipment fakeShipment = new Shipment();
            fakeShipment.setTrackingNumber("ABC123");
            when(shipmentPersistenceService.save(any(), any(), any(), any())).thenReturn(fakeShipment);

            Shipment result = shipmentService.createShipment(dto, "testuser");
            assertEquals("ABC123", result.getTrackingNumber());
            verify(geocodingService, times(2)).geocode(anyString());
            verify(shipmentPersistenceService).save(eq("testuser"), eq(dto), eq(fakeCoords), eq(fakeCoords));
        }

        @Test
        void shouldHandleNullCoordsFromGeocoder(){
            CreateShipmentDto dto = new CreateShipmentDto(makeAddress(), makeAddress(), List.of());

            when(geocodingService.geocode(anyString())).thenReturn(null);

            Shipment fakeShipment = new Shipment();
            fakeShipment.setTrackingNumber("XYZ999");
            when(shipmentPersistenceService.save(any(), any(), isNull(), isNull())).thenReturn(fakeShipment);

            Shipment result = shipmentService.createShipment(dto, "testuser");
            assertEquals("XYZ999", result.getTrackingNumber());
        }
    }

    @Nested
    class testGetMyShipments{
        @Test
        void shouldReturnShipmentsForUser(){
            User fakeUser = new User();
            fakeUser.setId(1L);
            fakeUser.setUsername("john");

            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(10L);

            when(userRepository.findByUsername("john")).thenReturn(Optional.of(fakeUser));
            when(shipmentRepository.findByShipperId(1L)).thenReturn(List.of(fakeShipment));

            List<Shipment> result = shipmentService.getMyShipments("john");

            assertEquals(1, result.size());
            assertEquals(10L, result.getFirst().getId());
        }

        @Test
        void shouldThrowNotFoundExceptionWhenUserNotFound(){
            assertThrows(NotFoundException.class, () -> shipmentService.getMyShipments("unknown"));
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

        @Test
        void shouldThrowInvalidTransitionExceptionWhenCancellingDelivered(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.DELIVERED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            assertThrows(InvalidTransitionException.class, () -> shipmentService.cancelShipment(1L));
            verify(shipmentRepository, never()).updateStatus(any(), any());
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

            shipmentService.updateShipmentStatus(1L, ShipmentStatus.PICKED_UP);

            verify(shipmentRepository).updateStatus(1L, ShipmentStatus.PICKED_UP);
        }

        @Test
        void shouldThrowNotFoundExceptionWhenShipmentNotFound(){
            assertThrows(NotFoundException.class, () -> shipmentService.updateShipmentStatus(1L, ShipmentStatus.PICKED_UP));
        }

        @Test
        void shouldThrowInvalidTransitionExceptionWhenTransitionIsInvalid(){
            Shipment fakeShipment = new Shipment();
            fakeShipment.setId(1L);
            fakeShipment.setStatus(ShipmentStatus.DELIVERED);

            when(shipmentRepository.findById(1L)).thenReturn(Optional.of(fakeShipment));

            assertThrows(InvalidTransitionException.class, () -> shipmentService.updateShipmentStatus(1L, ShipmentStatus.CREATED));
            verify(shipmentRepository, never()).updateStatus(any(), any());
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
