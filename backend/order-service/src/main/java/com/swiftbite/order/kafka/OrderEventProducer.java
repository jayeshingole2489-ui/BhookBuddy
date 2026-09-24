package com.swiftbite.order.kafka;

import com.swiftbite.order.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(OrderEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ORDER_EVENTS_TOPIC, event.getOrderId(), event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish order event [{}] for order [{}]: {}",
                            event.getEventType(), event.getOrderId(), ex.getMessage(), ex);
                } else {
                    logger.info("Published order event [{}] for order [{}] to partition [{}] at offset [{}]",
                            event.getEventType(),
                            event.getOrderId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            // Log but don't propagate — Kafka being down should not crash the order service
            logger.error("Exception while publishing order event [{}] for order [{}]: {}",
                    event.getEventType(), event.getOrderId(), ex.getMessage(), ex);
        }
    }
}
