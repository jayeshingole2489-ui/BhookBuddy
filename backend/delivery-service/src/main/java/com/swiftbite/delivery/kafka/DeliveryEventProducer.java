package com.swiftbite.delivery.kafka;

import com.swiftbite.delivery.dto.DeliveryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryEventProducer.class);
    private static final String TOPIC = "delivery-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDeliveryEvent(String key, DeliveryEvent event) {
        logger.info("Publishing delivery event: {} for order: {}", event.getEventType(), event.getOrderId());
        kafkaTemplate.send(TOPIC, key, event);
    }
}
