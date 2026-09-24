package com.swiftbite.notification.kafka;

import com.swiftbite.notification.dto.DeliveryEvent;
import com.swiftbite.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final NotificationService notificationService;

    public DeliveryEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "delivery-events", groupId = "notification-service-group")
    public void handleDeliveryEvent(DeliveryEvent event) {
        try {
            notificationService.handleDeliveryEvent(event);
        } catch (Exception e) {
            logger.error("Error processing delivery event for notification: {}", e.getMessage(), e);
        }
    }
}
