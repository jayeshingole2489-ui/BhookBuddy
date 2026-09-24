package com.swiftbite.notification.kafka;

import com.swiftbite.notification.dto.OrderEvent;
import com.swiftbite.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service-group")
    public void handleOrderEvent(OrderEvent event) {
        try {
            notificationService.handleOrderEvent(event);
        } catch (Exception e) {
            logger.error("Error processing order event for notification: {}", e.getMessage(), e);
        }
    }
}
