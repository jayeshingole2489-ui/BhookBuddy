package com.swiftbite.notification.service;

import com.swiftbite.notification.dto.DeliveryEvent;
import com.swiftbite.notification.dto.NotificationResponse;
import com.swiftbite.notification.dto.OrderEvent;
import com.swiftbite.notification.dto.PaymentEvent;
import com.swiftbite.notification.entity.Notification;
import com.swiftbite.notification.entity.NotificationType;
import com.swiftbite.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(Long userId, String orderId, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void handleOrderEvent(OrderEvent event) {
        if (event == null || event.getUserId() == null) {
            logger.warn("Received null or invalid order event");
            return;
        }
        String restaurantName = event.getRestaurantName() != null ? event.getRestaurantName() : "the restaurant";
        switch (event.getEventType()) {
            case "ORDER_CREATED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.ORDER_PLACED,
                        "Order Placed!",
                        "Your order from " + restaurantName + " has been placed.");
                logger.info("ORDER_PLACED notification created for user {}", event.getUserId());
            }
            case "ORDER_CONFIRMED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.ORDER_CONFIRMED,
                        "Order Confirmed!",
                        "Great news! Your order has been confirmed.");
                logger.info("ORDER_CONFIRMED notification created for user {}", event.getUserId());
            }
            case "ORDER_CANCELLED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.ORDER_CANCELLED,
                        "Order Cancelled",
                        "Your order has been cancelled.");
                logger.info("ORDER_CANCELLED notification created for user {}", event.getUserId());
            }
            case "ORDER_DELIVERED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.ORDER_DELIVERED,
                        "Delivered!",
                        "Your order has been delivered. Enjoy your meal!");
                logger.info("ORDER_DELIVERED notification created for user {}", event.getUserId());
            }
            default -> logger.debug("Ignoring order event type: {}", event.getEventType());
        }
    }

    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        if (event == null || event.getUserId() == null) {
            logger.warn("Received null or invalid payment event");
            return;
        }
        String amountStr = event.getAmount() != null ? event.getAmount().toPlainString() : "0";
        switch (event.getEventType()) {
            case "PAYMENT_COMPLETED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.PAYMENT_SUCCESS,
                        "Payment Successful",
                        "Payment of ₹" + amountStr + " confirmed.");
                logger.info("PAYMENT_SUCCESS notification created for user {}", event.getUserId());
            }
            case "PAYMENT_FAILED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.PAYMENT_FAILED,
                        "Payment Failed",
                        "Your payment could not be processed. Please try again.");
                logger.info("PAYMENT_FAILED notification created for user {}", event.getUserId());
            }
            case "PAYMENT_REFUNDED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.REFUND_INITIATED,
                        "Refund Initiated",
                        "Refund of ₹" + amountStr + " has been initiated.");
                logger.info("REFUND_INITIATED notification created for user {}", event.getUserId());
            }
            default -> logger.debug("Ignoring payment event type: {}", event.getEventType());
        }
    }

    @Transactional
    public void handleDeliveryEvent(DeliveryEvent event) {
        if (event == null || event.getUserId() == null) {
            logger.warn("Received null or invalid delivery event");
            return;
        }
        String personName = event.getDeliveryPersonName() != null ? event.getDeliveryPersonName() : "A delivery partner";
        switch (event.getEventType()) {
            case "DELIVERY_ASSIGNED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.DELIVERY_ASSIGNED,
                        "Delivery Partner Assigned",
                        personName + " will deliver your order.");
                logger.info("DELIVERY_ASSIGNED notification created for user {}", event.getUserId());
            }
            case "DELIVERY_PICKED_UP", "DELIVERY_OUT_FOR_DELIVERY", "DELIVERY_STATUS_UPDATED" -> {
                if ("DELIVERED".equals(event.getStatus())) {
                    createNotification(event.getUserId(), event.getOrderId(),
                            NotificationType.ORDER_DELIVERED,
                            "Delivered!",
                            "Your food has arrived!");
                    logger.info("ORDER_DELIVERED notification created for user {}", event.getUserId());
                } else if ("OUT_FOR_DELIVERY".equals(event.getStatus()) || "PICKED_UP".equals(event.getStatus())) {
                    createNotification(event.getUserId(), event.getOrderId(),
                            NotificationType.OUT_FOR_DELIVERY,
                            "Out for Delivery!",
                            "Your order is on the way!");
                    logger.info("OUT_FOR_DELIVERY notification created for user {}", event.getUserId());
                }
            }
            case "DELIVERY_DELIVERED" -> {
                createNotification(event.getUserId(), event.getOrderId(),
                        NotificationType.ORDER_DELIVERED,
                        "Delivered!",
                        "Your food has arrived!");
                logger.info("DELIVERY_DELIVERED notification created for user {}", event.getUserId());
            }
            default -> logger.debug("Ignoring delivery event type: {}", event.getEventType());
        }
    }

    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public int getUnreadCount(Long userId) {
        return (int) notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        notificationRepository.delete(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setOrderId(notification.getOrderId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}
