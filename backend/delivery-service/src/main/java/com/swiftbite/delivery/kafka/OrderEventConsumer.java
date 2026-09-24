package com.swiftbite.delivery.kafka;

import com.swiftbite.delivery.dto.OrderEvent;
import com.swiftbite.delivery.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final DeliveryService deliveryService;

    public OrderEventConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "order-events", groupId = "delivery-service-group")
    public void handleOrderEvent(OrderEvent event) {
        try {
            if (event == null || event.getEventType() == null) {
                logger.warn("Received null or invalid order event");
                return;
            }
            logger.info("Received order event: {} for order: {}", event.getEventType(), event.getOrderId());
            switch (event.getEventType()) {
                case "ORDER_CONFIRMED" -> deliveryService.createDelivery(event);
                case "ORDER_CANCELLED" -> {
                    if (event.getOrderId() != null) {
                        deliveryService.cancelDelivery(event.getOrderId());
                    }
                }
                default -> logger.debug("Ignoring order event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing order event: {}", e.getMessage(), e);
        }
    }
}
