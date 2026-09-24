package com.swiftbite.delivery.service;

import com.swiftbite.delivery.dto.DeliveryResponse;
import com.swiftbite.delivery.dto.OrderEvent;
import com.swiftbite.delivery.entity.Delivery;
import com.swiftbite.delivery.entity.DeliveryStatus;
import com.swiftbite.delivery.kafka.DeliveryEventProducer;
import com.swiftbite.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryEventProducer deliveryEventProducer;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void createDelivery_fromOrderEvent() {
        OrderEvent event = new OrderEvent();
        event.setOrderId("order-123");
        event.setUserId(1L);
        event.setRestaurantName("Test Restaurant");
        event.setDeliveryAddress("123 Main St");

        when(deliveryRepository.findByOrderId("order-123")).thenReturn(Optional.empty());
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        DeliveryResponse response = deliveryService.createDelivery(event);

        assertNotNull(response);
        assertEquals("order-123", response.getOrderId());
        assertEquals(DeliveryStatus.ASSIGNED, response.getStatus());
        assertNotNull(response.getDeliveryPersonName());
        verify(deliveryRepository, atLeast(2)).save(any(Delivery.class));
        verify(deliveryEventProducer, atLeast(2)).publishDeliveryEvent(any(), any());
    }

    @Test
    void updateDeliveryStatus_validTransition() {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(UUID.randomUUID().toString());
        delivery.setOrderId("order-123");
        delivery.setUserId(1L);
        delivery.setStatus(DeliveryStatus.ASSIGNED);

        when(deliveryRepository.findByDeliveryId(any())).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryResponse response = deliveryService.updateDeliveryStatus(delivery.getDeliveryId(), DeliveryStatus.PICKED_UP);

        assertEquals(DeliveryStatus.PICKED_UP, response.getStatus());
        assertNotNull(response.getPickedUpAt());
    }

    @Test
    void updateDeliveryStatus_invalidTransition_throwsException() {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(UUID.randomUUID().toString());
        delivery.setStatus(DeliveryStatus.DELIVERED);

        when(deliveryRepository.findByDeliveryId(any())).thenReturn(Optional.of(delivery));

        assertThrows(IllegalStateException.class, () ->
                deliveryService.updateDeliveryStatus(delivery.getDeliveryId(), DeliveryStatus.PICKED_UP));
    }
}
