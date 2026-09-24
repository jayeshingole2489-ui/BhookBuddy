package com.swiftbite.payment.kafka;

import com.swiftbite.payment.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentEvent(String key, PaymentEvent event) {
        logger.info("Publishing payment event: {} for order: {}", event.getEventType(), event.getOrderId());
        kafkaTemplate.send(TOPIC, key, event);
    }
}
