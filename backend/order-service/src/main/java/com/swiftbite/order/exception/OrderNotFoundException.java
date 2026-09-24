package com.swiftbite.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String orderId, Long userId) {
        super("Order not found with id: " + orderId + " for user: " + userId);
    }
}
