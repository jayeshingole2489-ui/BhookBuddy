package com.swiftbite.order.kafka;

import com.swiftbite.order.event.PaymentEvent;
import com.swiftbite.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderService orderService;

    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(PaymentEvent event) {
        try {
            logger.info("Received payment event [{}] for order [{}]",
                    event.getEventType(), event.getOrderId());
            orderService.processPaymentEvent(event);
        } catch (Exception ex) {
            // Log error but do not rethrow — prevents infinite retry loop for unrecoverable errors
            logger.error("Error processing payment event [{}] for order [{}]: {}",
                    event.getEventType(), event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
