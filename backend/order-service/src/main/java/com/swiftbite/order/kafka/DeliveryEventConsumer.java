package com.swiftbite.order.kafka;

import com.swiftbite.order.event.DeliveryEvent;
import com.swiftbite.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final OrderService orderService;

    public DeliveryEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "delivery-events", groupId = "order-service-group")
    public void handleDeliveryEvent(DeliveryEvent event) {
        try {
            logger.info("Received delivery event [{}] for order [{}]",
                    event.getEventType(), event.getOrderId());
            orderService.processDeliveryEvent(event);
        } catch (Exception ex) {
            // Log error but do not rethrow — prevents infinite retry loop for unrecoverable errors
            logger.error("Error processing delivery event [{}] for order [{}]: {}",
                    event.getEventType(), event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
