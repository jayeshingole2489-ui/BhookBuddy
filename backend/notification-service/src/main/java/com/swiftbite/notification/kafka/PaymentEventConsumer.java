package com.swiftbite.notification.kafka;

import com.swiftbite.notification.dto.PaymentEvent;
import com.swiftbite.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final NotificationService notificationService;

    public PaymentEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "payment-events", groupId = "notification-service-group")
    public void handlePaymentEvent(PaymentEvent event) {
        try {
            notificationService.handlePaymentEvent(event);
        } catch (Exception e) {
            logger.error("Error processing payment event for notification: {}", e.getMessage(), e);
        }
    }
}
