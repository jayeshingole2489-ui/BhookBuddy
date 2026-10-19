package com.swiftbite.delivery.service;

import com.swiftbite.delivery.dto.DeliveryEvent;
import com.swiftbite.delivery.dto.DeliveryResponse;
import com.swiftbite.delivery.dto.OrderEvent;
import com.swiftbite.delivery.entity.Delivery;
import com.swiftbite.delivery.entity.DeliveryStatus;
import com.swiftbite.delivery.kafka.DeliveryEventProducer;
import com.swiftbite.delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private static final List<String> DELIVERY_PERSONS = Arrays.asList(
            "Raj Kumar", "Priya Nair", "Arjun Singh", "Meena Devi", "Vikram Rao"
    );

    private static final List<String> DELIVERY_PHONES = Arrays.asList(
            "+91-9876543210", "+91-9876543211", "+91-9876543212", "+91-9876543213", "+91-9876543214"
    );

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventProducer deliveryEventProducer;
    private final Random random = new Random();

    public DeliveryService(DeliveryRepository deliveryRepository, DeliveryEventProducer deliveryEventProducer) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryEventProducer = deliveryEventProducer;
    }

    @Transactional
    public DeliveryResponse createDelivery(OrderEvent event) {
        // Check if delivery already exists for this order
        if (deliveryRepository.findByOrderId(event.getOrderId()).isPresent()) {
            logger.warn("Delivery already exists for order: {}", event.getOrderId());
            return toResponse(deliveryRepository.findByOrderId(event.getOrderId()).get());
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(UUID.randomUUID().toString());
        delivery.setOrderId(event.getOrderId());
        delivery.setUserId(event.getUserId());
        delivery.setRestaurantName(event.getRestaurantName());
        delivery.setDeliveryAddress(event.getDeliveryAddress());
        delivery.setStatus(DeliveryStatus.WAITING_FOR_ASSIGNMENT);
        delivery = deliveryRepository.save(delivery);

        logger.info("Delivery created for order: {}", event.getOrderId());
        publishDeliveryEvent(delivery, "DELIVERY_CREATED");

        // Auto-assign a delivery person
        int idx = random.nextInt(DELIVERY_PERSONS.size());
        delivery.setDeliveryPersonName(DELIVERY_PERSONS.get(idx));
        delivery.setDeliveryPersonPhone(DELIVERY_PHONES.get(idx));
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        delivery.setEstimatedMinutes(20 + random.nextInt(20)); // 20-40 minutes
        delivery = deliveryRepository.save(delivery);

        logger.info("Delivery assigned to {} for order: {}", delivery.getDeliveryPersonName(), event.getOrderId());
        publishDeliveryEvent(delivery, "DELIVERY_ASSIGNED");

        return toResponse(delivery);
    }

    public DeliveryResponse getDeliveryByOrderId(String orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + orderId));
        return toResponse(delivery);
    }

    public DeliveryResponse getDeliveryById(String deliveryId) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));
        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(String deliveryId, DeliveryStatus newStatus) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        validateStatusTransition(delivery.getStatus(), newStatus);

        delivery.setStatus(newStatus);
        if (newStatus == DeliveryStatus.PICKED_UP) {
            delivery.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
        }

        delivery = deliveryRepository.save(delivery);
        logger.info("Delivery {} status updated to {}", deliveryId, newStatus);

        String eventType = switch (newStatus) {
            case PICKED_UP -> "DELIVERY_PICKED_UP";
            case DELIVERED -> "DELIVERY_DELIVERED";
            case CANCELLED -> "DELIVERY_CANCELLED";
            case OUT_FOR_DELIVERY -> "DELIVERY_OUT_FOR_DELIVERY";
            case ASSIGNED -> "DELIVERY_ASSIGNED";
            default -> "DELIVERY_STATUS_UPDATED";
        };
        publishDeliveryEvent(delivery, eventType);

        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse assignDeliveryPerson(String deliveryId, String name, String phone) {
        Delivery delivery = deliveryRepository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + deliveryId));

        delivery.setDeliveryPersonName(name);
        delivery.setDeliveryPersonPhone(phone);
        if (delivery.getStatus() == DeliveryStatus.WAITING_FOR_ASSIGNMENT) {
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            delivery.setAssignedAt(LocalDateTime.now());
        }
        delivery = deliveryRepository.save(delivery);
        logger.info("Delivery {} assigned to {} ({})", deliveryId, name, phone);
        publishDeliveryEvent(delivery, "DELIVERY_ASSIGNED");

        return toResponse(delivery);
    }

    public List<DeliveryResponse> getAllDeliveries() {
        return deliveryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelDelivery(String orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresent(delivery -> {
            if (delivery.getStatus() != DeliveryStatus.DELIVERED && delivery.getStatus() != DeliveryStatus.CANCELLED) {
                delivery.setStatus(DeliveryStatus.CANCELLED);
                deliveryRepository.save(delivery);
                publishDeliveryEvent(delivery, "DELIVERY_CANCELLED");
                logger.info("Delivery cancelled for order: {}", orderId);
            }
        });
    }

    private void validateStatusTransition(DeliveryStatus current, DeliveryStatus next) {
        boolean valid = switch (current) {
            case WAITING_FOR_ASSIGNMENT -> next == DeliveryStatus.ASSIGNED || next == DeliveryStatus.CANCELLED;
            case ASSIGNED -> next == DeliveryStatus.PICKED_UP || next == DeliveryStatus.CANCELLED;
            case PICKED_UP -> next == DeliveryStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DeliveryStatus.DELIVERED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void publishDeliveryEvent(Delivery delivery, String eventType) {
        DeliveryEvent event = new DeliveryEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setOrderId(delivery.getOrderId());
        event.setDeliveryId(delivery.getDeliveryId());
        event.setUserId(delivery.getUserId());
        event.setDeliveryPersonName(delivery.getDeliveryPersonName());
        event.setStatus(delivery.getStatus().name());
        event.setEstimatedMinutes(delivery.getEstimatedMinutes());
        event.setTimestamp(LocalDateTime.now());
        deliveryEventProducer.publishDeliveryEvent(delivery.getOrderId(), event);
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setDeliveryId(delivery.getDeliveryId());
        response.setOrderId(delivery.getOrderId());
        response.setUserId(delivery.getUserId());
        response.setRestaurantAddress(delivery.getRestaurantAddress());
        response.setRestaurantName(delivery.getRestaurantName());
        response.setDeliveryAddress(delivery.getDeliveryAddress());
        response.setStatus(delivery.getStatus());
        response.setDeliveryPersonName(delivery.getDeliveryPersonName());
        response.setDeliveryPersonPhone(delivery.getDeliveryPersonPhone());
        response.setEstimatedMinutes(delivery.getEstimatedMinutes());
        response.setAssignedAt(delivery.getAssignedAt());
        response.setPickedUpAt(delivery.getPickedUpAt());
        response.setDeliveredAt(delivery.getDeliveredAt());
        response.setCreatedAt(delivery.getCreatedAt());
        return response;
    }
}
