package com.swiftbite.notification.service;

import com.swiftbite.notification.dto.DeliveryEvent;
import com.swiftbite.notification.dto.OrderEvent;
import com.swiftbite.notification.dto.PaymentEvent;
import com.swiftbite.notification.entity.Notification;
import com.swiftbite.notification.entity.NotificationType;
import com.swiftbite.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void handleOrderEvent_orderCreated() {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CREATED");
        event.setOrderId("order-123");
        event.setUserId(1L);
        event.setRestaurantName("Pizza Palace");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        notificationService.handleOrderEvent(event);

        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getType() == NotificationType.ORDER_PLACED &&
                n.getUserId().equals(1L) &&
                n.getMessage().contains("Pizza Palace")
        ));
    }

    @Test
    void handlePaymentEvent_paymentSuccess() {
        PaymentEvent event = new PaymentEvent();
        event.setEventType("PAYMENT_COMPLETED");
        event.setOrderId("order-123");
        event.setUserId(1L);
        event.setAmount(new BigDecimal("250.00"));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        notificationService.handlePaymentEvent(event);

        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getType() == NotificationType.PAYMENT_SUCCESS &&
                n.getMessage().contains("250.00")
        ));
    }

    @Test
    void handlePaymentEvent_paymentFailed() {
        PaymentEvent event = new PaymentEvent();
        event.setEventType("PAYMENT_FAILED");
        event.setOrderId("order-123");
        event.setUserId(1L);
        event.setAmount(new BigDecimal("100.00"));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        notificationService.handlePaymentEvent(event);

        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getType() == NotificationType.PAYMENT_FAILED
        ));
    }

    @Test
    void handleDeliveryEvent_deliveryAssigned() {
        DeliveryEvent event = new DeliveryEvent();
        event.setEventType("DELIVERY_ASSIGNED");
        event.setOrderId("order-123");
        event.setUserId(1L);
        event.setDeliveryPersonName("Raj Kumar");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        notificationService.handleDeliveryEvent(event);

        verify(notificationRepository, times(1)).save(argThat(n ->
                n.getType() == NotificationType.DELIVERY_ASSIGNED &&
                n.getMessage().contains("Raj Kumar")
        ));
    }
}
